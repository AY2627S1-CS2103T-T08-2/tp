package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;

public class ListProjectsCommandTest {

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListProjectsCommand().execute(null));
    }

    @Test
    public void execute_emptyProjectList_returnsEmptyMessage() throws CommandException {
        Model model = new ModelManager();

        CommandResult result = new ListProjectsCommand().execute(model);

        assertEquals(ListProjectsCommand.MESSAGE_EMPTY, result.getFeedbackToUser());
        assertEquals(CommandResult.ListViewTarget.PROJECTS, result.getListViewTarget());
    }

    @Test
    public void execute_projects_returnsProjectsInListOrder() throws CommandException {
        Model model = new ModelManager();
        Project first = createProject("CS2103T", Description.EMPTY);
        Project second = createProject("Orbital", new Description("Build an app"));
        model.addProject(first);
        model.addProject(second);

        CommandResult result = new ListProjectsCommand().execute(model);

        assertEquals("Listed 2 project(s):\n"
                + "1. CS2103T | Deadline: 2026-11-13 | Description: No description provided\n"
                + "2. Orbital | Deadline: 2026-11-13 | Description: Build an app",
            result.getFeedbackToUser());
    }

    @Test
    public void constructor_nullContactIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListProjectsCommand(null));
    }

    @Test
    public void execute_invalidContactIndex_throwsCommandException() {
        Model model = new ModelManager();
        model.addPerson(ALICE);

        assertThrows(CommandException.class,
            Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () -> new ListProjectsCommand(
                Index.fromOneBased(2)).execute(model));
    }

    @Test
    public void execute_contactWithoutProjects_returnsEmptyMessage() throws CommandException {
        Model model = new ModelManager();
        model.addPerson(ALICE);

        CommandResult result = new ListProjectsCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(String.format(ListProjectsCommand.MESSAGE_NO_PROJECTS, ALICE.getName()),
            result.getFeedbackToUser());
    }

    @Test
    public void execute_filteredContact_preservesProjectIndicesAndModel() throws CommandException {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BOB);
        Project first = createProject("Unrelated", Description.EMPTY);
        Project second = createProject("CS2103T", Description.EMPTY);
        Project third = createProject("Orbital", new Description("Build an app"));
        model.addProject(first);
        model.addProject(second);
        model.addProject(third);
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), first.getProjectId()));
        model.addProjectMembership(new ProjectMembership(BOB.getPersonId(), third.getProjectId()));
        model.addProjectMembership(new ProjectMembership(BOB.getPersonId(), second.getProjectId()));
        model.updateFilteredPersonList(person -> person
            .getPersonId()
            .equals(BOB.getPersonId()));
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        Predicate<? super Person> originalFilter = model.getFilteredPersonListPredicate();

        CommandResult result = new ListProjectsCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(String.format("Projects for contact \"%1$s\" (2):\n", BOB.getName())
                + "2. CS2103T | Deadline: 2026-11-13 | Description: No description provided\n"
                + "3. Orbital | Deadline: 2026-11-13 | Description: Build an app",
            result.getFeedbackToUser());
        assertEquals(originalAddressBook, model.getAddressBook());
        assertEquals(originalFilter, model.getFilteredPersonListPredicate());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
    }

    private Project createProject(String name, Description description) {
        return new Project(new ProjectName(name),
            new Deadline(LocalDate.of(2026, 11, 13)), description);
    }

    @Test
    public void execute_contactQuery_preservesProjectFilterAndUsesVisibleIndices() throws CommandException {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BOB);
        Project hidden = createProject("Alpha", Description.EMPTY);
        Project visible = createProject("Beta", Description.EMPTY);
        model.addProject(hidden);
        model.addProject(visible);
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), hidden.getProjectId()));
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), visible.getProjectId()));
        model.updateFilteredProjectList(project -> project.equals(visible));
        Predicate<? super Project> originalFilter = model.getFilteredProjectListPredicate();
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());

        CommandResult result = new ListProjectsCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(String.format("Projects for contact \"%1$s\" (2):\n", ALICE.getName())
                + "[not displayed] Alpha | Deadline: 2026-11-13 | Description: No description provided\n"
                + "1. Beta | Deadline: 2026-11-13 | Description: No description provided",
            result.getFeedbackToUser());
        assertEquals(CommandResult.ListViewTarget.NONE, result.getListViewTarget());
        assertEquals(List.of(visible), model.getFilteredProjectList());
        assertEquals(originalFilter, model.getFilteredProjectListPredicate());

        new ListProjectsCommand(Index.fromOneBased(2)).execute(model);
        assertEquals(List.of(visible), model.getFilteredProjectList());
        assertEquals(originalFilter, model.getFilteredProjectListPredicate());
        assertEquals(originalAddressBook, model.getAddressBook());

        model.updateFilteredProjectList(project -> false);
        new ListProjectsCommand(Index.fromOneBased(1)).execute(model);
        assertEquals(List.of(), model.getFilteredProjectList());
    }
}
