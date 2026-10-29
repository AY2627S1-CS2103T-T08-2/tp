package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.person.Person;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;

public class ListMembersCommandTest {

    private final Project project = new Project(new ProjectName("CS2103T Team Project"),
        new Deadline(LocalDate.of(2026, 11, 13)), Description.EMPTY);

    @Test
    public void constructor_nullProjectName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListMembersCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListMembersCommand(project.getName()).execute(null));
    }

    @Test
    public void execute_unknownProject_throwsCommandException() {
        Model model = new ModelManager();
        ListMembersCommand command = new ListMembersCommand(project.getName());
        String expectedMessage = String.format(ListMembersCommand.MESSAGE_PROJECT_NOT_FOUND, project.getName());

        assertThrows(CommandException.class, expectedMessage, () -> command.execute(model));
    }

    @Test
    public void execute_projectWithoutMembers_returnsEmptyMessage() throws Exception {
        Model model = new ModelManager();
        model.addProject(project);

        CommandResult result = new ListMembersCommand(project.getName()).execute(model);
        String expectedMessage = String.format(ListMembersCommand.MESSAGE_NO_MEMBERS, project.getName());

        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(CommandResult.ListViewTarget.CONTACTS, result.getListViewTarget());
    }

    @Test
    public void execute_projectWithMembers_listsNamesInContactOrder() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addProject(project);
        model.addProjectMembership(new ProjectMembership(BENSON.getPersonId(), project.getProjectId()));
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), project.getProjectId()));

        CommandResult result = new ListMembersCommand(project.getName()).execute(model);

        String expectedMessage = "Contacts in project \"CS2103T Team Project\" (2):\n1. Alice Pauline\n2. Benson Meier";
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        assertEquals(CommandResult.ListViewTarget.CONTACTS, result.getListViewTarget());
    }

    @Test
    public void execute_filtersCardsAndListRestoresAllContacts() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addProject(project);
        model.addProjectMembership(new ProjectMembership(BENSON.getPersonId(), project.getProjectId()));
        model.updateFilteredPersonList(person -> person.equals(ALICE));

        new ListMembersCommand(project.getName()).execute(model);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());

        new ListCommand().execute(model);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_membershipChanges_refreshDisplayedCards() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addProject(project);
        new ListMembersCommand(project.getName()).execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());
        ProjectMembership membership = new ProjectMembership(ALICE.getPersonId(), project.getProjectId());

        model.addProjectMembership(membership);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        model.removeProjectMembership(membership);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_inconsistentMemberFilter_throwsAssertionError() {
        Model model = new ModelManager() {
            @Override
            public void updateFilteredPersonList(Predicate<Person> predicate) {
                // Simulate a faulty model that ignores the requested project-member filter.
            }
        };
        model.addPerson(ALICE);
        model.addProject(project);

        ListMembersCommand command = new ListMembersCommand(project.getName());
        String expectedMessage = "Displayed members must match project membership";
        assertThrows(AssertionError.class, expectedMessage, () -> command.execute(model));
    }

    @Test
    public void execute_unknownProject_preservesCurrentCards() {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        assertThrows(CommandException.class, () -> new ListMembersCommand(project.getName()).execute(model));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_deleteUsesDisplayedMemberIndex() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addProject(project);
        model.addProjectMembership(new ProjectMembership(BENSON.getPersonId(), project.getProjectId()));
        new ListMembersCommand(project.getName()).execute(model);

        new DeleteCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(ALICE), model
            .getAddressBook()
            .getPersonList());
        assertTrue(model
            .getFilteredPersonList()
            .isEmpty());
    }

    @Test
    public void execute_projectWithRole_listsRoleBesideMember() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addProject(project);
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), project.getProjectId(),
            new Role("Implement parser")));

        CommandResult result = new ListMembersCommand(project.getName()).execute(model);

        assertEquals("Contacts in project \"CS2103T Team Project\" (1):\n1. Alice Pauline — Implement parser",
            result.getFeedbackToUser());
    }

    @Test
    public void equals() {
        ListMembersCommand command = new ListMembersCommand(project.getName());

        assertTrue(command.equals(command));
        assertTrue(command.equals(new ListMembersCommand(new ProjectName("cs2103t team project"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertFalse(command.equals(new ListMembersCommand(new ProjectName("Orbital"))));
    }

    @Test
    public void toStringMethod() {
        ListMembersCommand command = new ListMembersCommand(project.getName());

        assertTrue(command
            .toString()
            .contains("projectName=CS2103T Team Project"));
    }
}
