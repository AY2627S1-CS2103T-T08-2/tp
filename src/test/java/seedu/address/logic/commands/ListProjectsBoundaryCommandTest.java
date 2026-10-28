package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.Project;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.ProjectBuilder;

/** Tests contact-project lookup against independent membership and display-order expectations. */
public class ListProjectsBoundaryCommandTest {
    private static final List<Person> PERSONS = IntStream.range(0, 3)
            .mapToObj(index -> new PersonBuilder().withName("Shared Name").withPhone("123" + index).build()).toList();
    private static final List<Project> PROJECTS = Stream.of("Zeta", "Alpha", "Beta")
            .map(name -> new ProjectBuilder().withName(name).withDeadline(LocalDate.of(2026, 11, 13))
                    .withDescription(name.equals("Alpha") ? "" : "Details " + name).build()).toList();
    private static final List<String> LINES = List.of(
            "1. Zeta | Deadline: 2026-11-13 | Description: Details Zeta",
            "2. Alpha | Deadline: 2026-11-13 | Description: No description provided",
            "3. Beta | Deadline: 2026-11-13 | Description: Details Beta");

    @TestFactory
    public Stream<DynamicTest> execute_allMembershipGraphsAndContactFilters_preservesGlobalProjectNumbers() {
        return IntStream.range(0, 512).mapToObj(graph -> dynamicTest("membership graph " + graph, () -> {
            ModelManager model = model(graph, 3);
            for (int filter = 0; filter < 8; filter++) {
                int mask = filter;
                List<Person> visible = IntStream.range(0, 3).filter(index -> (mask & (1 << index)) != 0)
                        .mapToObj(PERSONS::get).toList();
                model.updateFilteredPersonList(visible::contains);
                for (int index = 0; index < visible.size(); index++) {
                    int personIndex = PERSONS.indexOf(visible.get(index));
                    List<String> lines = IntStream.range(0, 3)
                            .filter(project -> (graph & (1 << (personIndex * 3 + project))) != 0)
                            .mapToObj(LINES::get).toList();
                    String expected = lines.isEmpty() ? "No projects are associated with contact \"Shared Name\"."
                            : "Projects for contact \"Shared Name\" (" + lines.size() + "):\n"
                                    + String.join("\n", lines);
                    assertReadOnlyResult(model, "listprojects c/" + (index + 1), new CommandResult(expected));
                }
                for (int invalid : new int[] {visible.size() + 1, Integer.MAX_VALUE}) {
                    AddressBook before = new AddressBook(model.getAddressBook());
                    Command command = new AddressBookParser().parseCommand("listprojects c/" + invalid);
                    assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                            assertThrows(CommandException.class, () -> command.execute(model)).getMessage());
                    assertUnchanged(before, visible, model);
                }
            }
        }));
    }

    @TestFactory
    public Stream<DynamicTest> execute_noArgumentsAllProjectCounts_ignoresContactFilter() {
        return IntStream.range(0, 4).boxed().flatMap(count -> IntStream.range(0, 8).mapToObj(filter ->
                dynamicTest("projects " + count + " filter " + filter, () -> {
                    ModelManager model = model(511, count);
                    model.updateFilteredPersonList(person -> (filter & (1 << PERSONS.indexOf(person))) != 0);
                    String expected = count == 0 ? "There are no projects to list."
                            : "Listed " + count + " project(s):\n" + String.join("\n", LINES.subList(0, count));
                    assertReadOnlyResult(model, "listprojects",
                            new CommandResult(expected, CommandResult.ListViewTarget.PROJECTS));
                })));
    }

    @Test
    public void execute_afterEditsAndRemoval_usesCurrentDetailsAndRenumberedProjects() throws Exception {
        ModelManager model = model(511, 3);
        Person renamed = new PersonBuilder(PERSONS.get(2)).withName("Renamed Contact").build();
        model.setPerson(PERSONS.get(2), renamed);
        Project renamedProject = new ProjectBuilder(PROJECTS.get(2)).withName("Renamed Project")
                .withDescription("x".repeat(200)).build();
        model.setProject(PROJECTS.get(2), renamedProject);
        model.deleteProject(PROJECTS.get(0));
        model.updateFilteredPersonList(person -> person.getPersonId().equals(renamed.getPersonId()));
        assertReadOnlyResult(model, "listprojects c/1", new CommandResult(
                "Projects for contact \"Renamed Contact\" (2):\n"
                        + "1. Alpha | Deadline: 2026-11-13 | Description: No description provided\n"
                        + "2. Renamed Project | Deadline: 2026-11-13 | Description: " + "x".repeat(200)));
        model.removeProjectMembership(new ProjectMembership(renamed.getPersonId(), PROJECTS.get(1).getProjectId()));
        model.removeProjectMembership(new ProjectMembership(renamed.getPersonId(), renamedProject.getProjectId()));
        assertReadOnlyResult(model, "listprojects c/1",
                new CommandResult("No projects are associated with contact \"Renamed Contact\"."));
    }

    @Test
    public void execute_emptyAddressBook_contactLookupRejectsAndUnfilteredLookupSucceeds() throws Exception {
        ModelManager model = new ModelManager();
        assertReadOnlyResult(model, "listprojects", new CommandResult(
                "There are no projects to list.", CommandResult.ListViewTarget.PROJECTS));
        Command command = new AddressBookParser().parseCommand("listprojects c/1");
        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                assertThrows(CommandException.class, () -> command.execute(model)).getMessage());
    }

    private static ModelManager model(int graph, int projectCount) {
        AddressBook book = new AddressBook();
        PERSONS.forEach(book::addPerson);
        PROJECTS.subList(0, projectCount).forEach(book::addProject);
        for (int index = 8; index >= 0; index--) {
            if ((graph & (1 << index)) != 0 && index % 3 < projectCount) {
                book.addProjectMembership(new ProjectMembership(PERSONS.get(index / 3).getPersonId(),
                        PROJECTS.get(index % 3).getProjectId(), new Role("Role " + index)));
            }
        }
        return new ModelManager(book, new UserPrefs());
    }

    private static void assertReadOnlyResult(ModelManager model, String input, CommandResult expected)
            throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> visible = new ArrayList<>(model.getFilteredPersonList());
        var predicate = model.getFilteredPersonListPredicate();
        assertEquals(expected, new AddressBookParser().parseCommand(input).execute(model));
        assertSame(predicate, model.getFilteredPersonListPredicate());
        assertUnchanged(before, visible, model);
    }

    private static void assertUnchanged(AddressBook before, List<Person> visible, ModelManager model) {
        assertEquals(before, model.getAddressBook());
        List<PersonId> ids = before.getPersonList().stream().map(Person::getPersonId).toList();
        assertEquals(ids, model.getAddressBook().getPersonList().stream().map(Person::getPersonId).toList());
        assertEquals(before.getProjectMembershipList().stream().map(ProjectMembership::getRole).toList(),
                model.getAddressBook().getProjectMembershipList().stream().map(ProjectMembership::getRole).toList());
        assertEquals(visible, model.getFilteredPersonList());
    }
}
