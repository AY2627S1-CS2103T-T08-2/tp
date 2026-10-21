package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
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
    }

    @Test
    public void execute_projectWithMemberRole_listsRoleBesideName() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addProject(project);
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), project.getProjectId())
                .withRole(new Role("Implement parser")));

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

        assertTrue(command.toString().contains("projectName=CS2103T Team Project"));
    }
}
