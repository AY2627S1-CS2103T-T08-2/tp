package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
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

/** Exercises association execution across filtered lists, index bounds, and existing pairs. */
public class AssociateBoundaryCommandTest {
    private static final List<Person> PERSONS = IntStream.range(0, 3)
            .mapToObj(index -> new PersonBuilder().withName("Shared Name").withPhone("123" + index)
                    .withPersonId(PersonId.fromString("00000000-0000-0000-0000-00000000000" + index)).build())
            .toList();
    private static final List<Project> PROJECTS = Stream.of("Zeta", "Alpha", "Beta")
            .map(name -> new ProjectBuilder().withName(name).build()).toList();
    private static final List<Integer> INDICES = List.of(1, 2, 3, 4, Integer.MAX_VALUE);

    @TestFactory
    public Stream<DynamicTest> execute_indexAndFilterPartitions_changesOnlyTheSelectedPair() {
        return IntStream.range(0, 8).boxed().flatMap(filter -> IntStream.range(0, 4).boxed()
                .flatMap(projectCount -> INDICES.stream().flatMap(contactIndex -> INDICES.stream()
                        .flatMap(projectIndex -> IntStream.range(0, 2).mapToObj(existing -> dynamicTest(
                                "filter " + filter + " projects " + projectCount + " c/" + contactIndex
                                        + " pr/" + projectIndex + " existing " + existing, () ->
                                        verifyAssociation(filter, projectCount, contactIndex, projectIndex,
                                        existing == 1)))))));
    }

    private static void verifyAssociation(int filter, int projectCount, int contactIndex, int projectIndex,
                                         boolean alreadyAssociated) throws Exception {
        AddressBook book = new AddressBook();
        PERSONS.forEach(book::addPerson);
        List<Project> projects = PROJECTS.subList(0, projectCount);
        projects.forEach(book::addProject);
        List<Person> visible = IntStream.range(0, 3).filter(index -> (filter & (1 << index)) != 0)
                .mapToObj(PERSONS::get).toList();
        boolean validContact = contactIndex <= visible.size();
        boolean validProject = projectIndex <= projects.size();
        ProjectMembership selected = validContact && validProject
                ? new ProjectMembership(visible.get(contactIndex - 1).getPersonId(),
                        projects.get(projectIndex - 1).getProjectId()) : null;
        for (Person person : PERSONS) {
            for (Project project : projects) {
                ProjectMembership pair = new ProjectMembership(person.getPersonId(), project.getProjectId(),
                        new Role("Preserve this role"));
                if (alreadyAssociated || !pair.equals(selected)) {
                    book.addProjectMembership(pair);
                }
            }
        }
        ModelManager model = new ModelManager(book, new UserPrefs());
        model.updateFilteredPersonList(visible::contains);
        List<ProjectMembership> expected = new ArrayList<>(book.getProjectMembershipList());
        Command command = new AddressBookParser().parseCommand(
                "associate c/" + contactIndex + " pr/" + projectIndex);
        if (!validContact || !validProject || alreadyAssociated) {
            String message = !validContact ? Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX
                    : !validProject ? Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX
                    : String.format(AssociateCommand.MESSAGE_DUPLICATE_ASSOCIATION,
                            visible.get(contactIndex - 1).getName(), projects.get(projectIndex - 1).getName());
            assertEquals(message, assertThrows(CommandException.class, () -> command.execute(model)).getMessage());
        } else {
            assertEquals(new CommandResult(String.format(AssociateCommand.MESSAGE_SUCCESS,
                    visible.get(contactIndex - 1).getName(), projects.get(projectIndex - 1).getName())),
                    command.execute(model));
            expected.add(selected);
            assertEquals(String.format(AssociateCommand.MESSAGE_DUPLICATE_ASSOCIATION,
                    visible.get(contactIndex - 1).getName(), projects.get(projectIndex - 1).getName()),
                    assertThrows(CommandException.class, () -> command.execute(model)).getMessage());
        }
        assertEquals(expected, model.getAddressBook().getProjectMembershipList());
        assertEquals(expected.stream().map(ProjectMembership::getRole).toList(), model.getAddressBook()
                .getProjectMembershipList().stream().map(ProjectMembership::getRole).toList());
        assertEquals(PERSONS, model.getAddressBook().getPersonList());
        assertEquals(PERSONS.stream().map(Person::getPersonId).toList(), model.getAddressBook().getPersonList()
                .stream().map(Person::getPersonId).toList());
        assertEquals(projects, model.getProjectList());
        assertEquals(visible, model.getFilteredPersonList());
    }
}
