package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;

public class SetRoleCommandTest {

    private final Project project = new Project(new ProjectName("CS2103T Team Project"),
            new Deadline(LocalDate.of(2026, 11, 13)), Description.EMPTY);

    @Test
    public void constructor_nullContactIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SetRoleCommand(null,
                Index.fromOneBased(1), new Role("Implement parser")));
    }

    @Test
    public void constructor_nullProjectIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SetRoleCommand(Index.fromOneBased(1), null,
                new Role("Implement parser")));
    }

    @Test
    public void constructor_nullRole_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SetRoleCommand(Index.fromOneBased(1),
                Index.fromOneBased(1), null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SetRoleCommand(Index.fromOneBased(1),
                Index.fromOneBased(1), new Role("Implement parser")).execute(null));
    }

    @Test
    public void execute_existingMembership_updatesRole() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addProject(project);
        ProjectMembership membership = new ProjectMembership(ALICE.getPersonId(), project.getProjectId());
        model.addProjectMembership(membership);

        CommandResult result = new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(1),
                new Role("Implement parser")).execute(model);

        assertEquals(new Role("Implement parser"), model.getAddressBook().getProjectMembershipList().get(0).getRole());
        assertEquals("Set role for \"Alice Pauline\" in project \"CS2103T Team Project\": Implement parser",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_afterListingMembers_usesDisplayedContactIndex() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addProject(project);
        model.addProjectMembership(new ProjectMembership(BENSON.getPersonId(), project.getProjectId()));
        new ListMembersCommand(project.getName()).execute(model);

        new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(1), new Role("Implement parser"))
                .execute(model);

        ProjectMembership updatedMembership = model.getAddressBook().getProjectMembershipList().get(0);
        assertEquals(BENSON.getPersonId(), updatedMembership.getPersonId());
        assertEquals(new Role("Implement parser"), updatedMembership.getRole());
    }

    @Test
    public void execute_unassociatedContact_throwsCommandException() {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addProject(project);

        assertThrows(CommandException.class, () -> new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(1),
                new Role("Implement parser")).execute(model));
    }

    @Test
    public void execute_invalidContactIndex_throwsCommandException() {
        Model model = new ModelManager();
        SetRoleCommand command = new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(1),
                new Role("Implement parser"));

        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                command.execute(model));
    }

    @Test
    public void execute_invalidProjectIndex_throwsCommandException() {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        SetRoleCommand command = new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(1),
                new Role("Implement parser"));

        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX, () ->
                command.execute(model));
    }

    @Test
    public void equals_differentValues_returnsFalse() {
        SetRoleCommand command = new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(1),
                new Role("Implement parser"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(1),
                new Role("Implement parser"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new Object()));
        assertFalse(command.equals(new SetRoleCommand(Index.fromOneBased(2), Index.fromOneBased(1),
                new Role("Implement parser"))));
        assertFalse(command.equals(new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(2),
                new Role("Implement parser"))));
        assertFalse(command.equals(new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(1),
                new Role("Write tests"))));
    }

    @Test
    public void toString_containsCommandFields() {
        SetRoleCommand command = new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(2),
                new Role("Implement parser"));

        assertTrue(command.toString().contains(
                "contactIndex=seedu.address.commons.core.index.Index{zeroBasedIndex=0}"));
        assertTrue(command.toString().contains(
                "projectIndex=seedu.address.commons.core.index.Index{zeroBasedIndex=1}"));
        assertTrue(command.toString().contains("role=Implement parser"));
    }
}
