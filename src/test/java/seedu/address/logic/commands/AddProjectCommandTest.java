package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;
import seedu.address.testutil.ProjectBuilder;

public class AddProjectCommandTest {

    @Test
    public void equals() {
        Project project = createProject("CS2103T", Description.EMPTY);
        Project copy = new ProjectBuilder(project).build();
        Project otherProject = createProject("Orbital", Description.EMPTY);
        Project editedProject = new ProjectBuilder(project)
                .withDescription("Different description")
                .build();

        AddProjectCommand command = new AddProjectCommand(project);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new AddProjectCommand(copy)));

        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertFalse(command.equals(new AddProjectCommand(otherProject)));
        assertFalse(command.equals(new AddProjectCommand(editedProject)));
    }

    @Test
    public void constructor_nullProject_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddProjectCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        Project project = createProject("CS2103T", Description.EMPTY);
        AddProjectCommand command = new AddProjectCommand(project);

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_validProject_addSuccessful() throws Exception {
        Model model = new ModelManager();
        Project project = createProject(
                "CS2103T", new Description("Build a contact manager"));

        CommandResult result = new AddProjectCommand(project).execute(model);

        assertEquals(List.of(project), model.getProjectList());
        assertEquals(
                "New project added:\nCS2103T\nDeadline: 2026-11-13\n"
                        + "Description: Build a contact manager",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_descriptionOmitted_addSuccessful() throws Exception {
        Model model = new ModelManager();
        Project project = createProject("CS2103T", Description.EMPTY);

        CommandResult result = new AddProjectCommand(project).execute(model);

        assertEquals(List.of(project), model.getProjectList());
        assertEquals(
                "New project added:\nCS2103T\nDeadline: 2026-11-13\n"
                        + "Description: No description provided",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_distinctProject_preservesExistingProjects() throws Exception {
        Model model = new ModelManager();
        Project existingProject = createProject("Orbital", Description.EMPTY);
        Project newProject = createProject("CS2103T", Description.EMPTY);
        model.addProject(existingProject);

        new AddProjectCommand(newProject).execute(model);

        assertEquals(List.of(existingProject, newProject), model.getProjectList());
    }

    @Test
    public void execute_duplicateProject_throwsCommandException() {
        Model model = new ModelManager();
        Project project = createProject("CS2103T", Description.EMPTY);
        model.addProject(project);

        AddProjectCommand command = new AddProjectCommand(project);

        assertThrows(
                CommandException.class,
                String.format(
                        AddProjectCommand.MESSAGE_DUPLICATE_PROJECT, project.getName()
                ), () -> command.execute(model)
        );

        assertEquals(List.of(project), model.getProjectList());
    }

    @Test
    public void execute_sameNameDifferentDetails_throwsCommandException() {
        Model model = new ModelManager();
        Project existingProject = createProject("CS2103T", Description.EMPTY);
        model.addProject(existingProject);

        Project duplicateProject = new Project(
                new ProjectName("CS2103T"),
                new Deadline(LocalDate.of(2027, 1, 1)),
                new Description("Different description"));

        AddProjectCommand command = new AddProjectCommand(duplicateProject);

        assertThrows(
                CommandException.class,
                String.format(
                        AddProjectCommand.MESSAGE_DUPLICATE_PROJECT, duplicateProject.getName()
                ), () -> command.execute(model)
        );

        assertEquals(List.of(existingProject), model.getProjectList());
    }

    @Test
    public void execute_sameNameDifferentCaseAndSpacing_throwsCommandException() {
        Model model = new ModelManager();
        Project existingProject = createProject("CS2103T Team Project", Description.EMPTY);
        model.addProject(existingProject);

        Project duplicateProject = createProject(
                "  cs2103t   team project  ", Description.EMPTY);
        AddProjectCommand command = new AddProjectCommand(duplicateProject);

        assertThrows(
                CommandException.class,
                String.format(
                        AddProjectCommand.MESSAGE_DUPLICATE_PROJECT, duplicateProject.getName()
                ), () -> command.execute(model)
        );

        assertEquals(List.of(existingProject), model.getProjectList());
    }

    private static Project createProject(String name, Description description) {
        return new Project(
                new ProjectName(name),
                new Deadline(LocalDate.of(2026, 11, 13)),
                description);
    }
}
