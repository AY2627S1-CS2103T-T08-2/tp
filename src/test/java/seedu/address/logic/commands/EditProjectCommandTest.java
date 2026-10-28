package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditProjectCommand.EditProjectDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.ProjectBuilder;

/**
 * Tests EditProjectCommand with the actual model.
 */
public class EditProjectCommandTest {

    private static final Index FIRST_PROJECT = Index.fromOneBased(1);
    private static final Index SECOND_PROJECT = Index.fromOneBased(2);
    private static final LocalDate NEW_DEADLINE = LocalDate.of(2027, 1, 1);

    private Model model;
    private Project firstProject;
    private Project secondProject;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        firstProject = new ProjectBuilder()
            .withName("CS2103T")
            .withDeadline(LocalDate.of(2026, 11, 13))
            .withDescription("Original description")
            .build();
        secondProject = new ProjectBuilder()
            .withName("Orbital Team")
            .withDeadline(LocalDate.of(2027, 7, 1))
            .withDescription("Another project")
            .build();

        model.addProject(firstProject);
        model.addProject(secondProject);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EditProjectCommand(null, new EditProjectDescriptor()));
        assertThrows(NullPointerException.class, () -> new EditProjectCommand(FIRST_PROJECT, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("New Name"));
        EditProjectCommand command = new EditProjectCommand(FIRST_PROJECT, descriptor);

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_allFieldsSpecified_success() throws Exception {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Updated Project"));
        descriptor.setDeadline(new Deadline(NEW_DEADLINE));
        descriptor.setDescription(new Description("Updated description"));

        Project expectedProject = new ProjectBuilder(firstProject)
            .withName("Updated Project")
            .withDeadline(NEW_DEADLINE)
            .withDescription("Updated description")
            .build();

        CommandResult result = new EditProjectCommand(FIRST_PROJECT, descriptor).execute(model);

        assertEquals(List.of(expectedProject, secondProject), model.getFilteredProjectList());
        assertEquals(firstProject.getProjectId(),
            model.getFilteredProjectList().getFirst().getProjectId());
        assertEquals(
            "Edited project: Updated Project; Deadline: 2027-01-01; "
                + "Description: Updated description",
            result.getFeedbackToUser());
    }

    @Test
    public void execute_nameOnly_preservesOtherFields() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Updated Project"));
        Project expectedProject = new ProjectBuilder(firstProject)
            .withName("Updated Project").build();

        assertEditSuccess(FIRST_PROJECT, descriptor, expectedProject);
    }

    @Test
    public void execute_deadlineOnly_preservesOtherFields() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setDeadline(new Deadline(NEW_DEADLINE));
        Project expectedProject = new ProjectBuilder(firstProject)
            .withDeadline(NEW_DEADLINE).build();

        assertEditSuccess(FIRST_PROJECT, descriptor, expectedProject);
    }

    @Test
    public void execute_descriptionOnly_preservesOtherFields() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setDescription(new Description("Updated description"));
        Project expectedProject = new ProjectBuilder(firstProject)
            .withDescription("Updated description").build();

        assertEditSuccess(FIRST_PROJECT, descriptor, expectedProject);
    }

    @Test
    public void execute_clearDescription_success() throws Exception {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setDescription(Description.EMPTY);
        Project expectedProject = new ProjectBuilder(firstProject)
            .withDescription("").build();

        CommandResult result = new EditProjectCommand(FIRST_PROJECT, descriptor).execute(model);

        assertEquals(List.of(expectedProject, secondProject), model.getFilteredProjectList());
        assertEquals(
            "Edited project: CS2103T; Deadline: 2026-11-13; "
                + "Description: No description provided",
            result.getFeedbackToUser());
    }

    @Test
    public void execute_secondProject_editsCorrectProject() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Updated Orbital"));
        Project expectedProject = new ProjectBuilder(secondProject)
            .withName("Updated Orbital").build();

        assertEditSuccess(SECOND_PROJECT, descriptor, expectedProject);
    }

    @Test
    public void execute_unchangedName_success() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(firstProject.getName());

        assertEditSuccess(FIRST_PROJECT, descriptor, firstProject);
    }

    @Test
    public void execute_duplicateName_failure() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(secondProject.getName());

        assertCommandFailure(
            new EditProjectCommand(FIRST_PROJECT, descriptor),
            model,
            EditProjectCommand.MESSAGE_DUPLICATE_PROJECT);
    }

    @Test
    public void execute_duplicateNameDifferentCaseAndSpacing_failure() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("  oRbItAl   tEaM  "));

        assertCommandFailure(
            new EditProjectCommand(FIRST_PROJECT, descriptor),
            model,
            EditProjectCommand.MESSAGE_DUPLICATE_PROJECT);
    }

    @Test
    public void execute_invalidIndex_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredProjectList().size() + 1);
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("New Name"));

        assertCommandFailure(
            new EditProjectCommand(invalidIndex, descriptor),
            model,
            Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyProjectList_failure() {
        model = new ModelManager();
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("New Name"));

        assertCommandFailure(
            new EditProjectCommand(FIRST_PROJECT, descriptor),
            model,
            Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_rename_preservesMemberships() throws Exception {
        Person contact = new PersonBuilder().build();
        model.addPerson(contact);
        ProjectMembership membership = new ProjectMembership(
            contact.getPersonId(), firstProject.getProjectId());
        model.addProjectMembership(membership);

        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Updated Project"));

        new EditProjectCommand(FIRST_PROJECT, descriptor).execute(model);

        Project editedProject = model.getFilteredProjectList().getFirst();
        assertEquals(firstProject.getProjectId(), editedProject.getProjectId());
        assertEquals(List.of(membership),
            model.getAddressBook().getProjectMembershipList());
        assertEquals(List.of(contact), model.getProjectMembers(editedProject));
        assertEquals(secondProject, model.getFilteredProjectList().get(1));
    }

    @Test
    public void execute_contactFilterActive_preservesContactFilter() throws Exception {
        Person shownContact = new PersonBuilder().withName("Alice").build();
        Person hiddenContact = new PersonBuilder().withName("Bob").build();
        model.addPerson(shownContact);
        model.addPerson(hiddenContact);
        model.updateFilteredPersonList(person ->
            person.getPersonId().equals(shownContact.getPersonId()));
        assertEquals(List.of(shownContact), model.getFilteredPersonList());

        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Updated Project"));

        new EditProjectCommand(FIRST_PROJECT, descriptor).execute(model);

        assertEquals(List.of(shownContact), model.getFilteredPersonList());
        assertEquals(new ProjectName("Updated Project"),
            model.getFilteredProjectList().getFirst().getName());
    }

    @Test
    public void constructor_descriptorModifiedAfterConstruction_usesDefensiveCopy() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Intended Name"));
        EditProjectCommand command = new EditProjectCommand(FIRST_PROJECT, descriptor);
        descriptor.setName(new ProjectName("Changed Afterwards"));

        Project expectedProject = new ProjectBuilder(firstProject)
            .withName("Intended Name").build();
        Model expectedModel = new ModelManager(
            new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setProject(firstProject, expectedProject);

        assertCommandSuccess(command, model,
            String.format(EditProjectCommand.MESSAGE_EDIT_PROJECT_SUCCESS,
                Messages.format(expectedProject)),
            expectedModel);
    }

    @Test
    public void equals() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Updated Project"));

        EditProjectCommand command =
            new EditProjectCommand(FIRST_PROJECT, descriptor);
        EditProjectCommand sameCommand = new EditProjectCommand(
            FIRST_PROJECT, new EditProjectDescriptor(descriptor));

        EditProjectDescriptor differentDescriptor = new EditProjectDescriptor();
        differentDescriptor.setName(new ProjectName("Another Name"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(sameCommand));
        assertTrue(sameCommand.equals(command));

        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertFalse(command.equals(
            new EditProjectCommand(SECOND_PROJECT, descriptor)));
        assertFalse(command.equals(
            new EditProjectCommand(FIRST_PROJECT, differentDescriptor)));
    }

    /**
     * Verifies the feedback and complete model state after a successful edit.
     */
    private void assertEditSuccess(Index index, EditProjectDescriptor descriptor, Project expectedProject) {
        Project target = model.getFilteredProjectList().get(index.getZeroBased());
        Model expectedModel = new ModelManager(
            new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setProject(target, expectedProject);

        assertCommandSuccess(
            new EditProjectCommand(index, descriptor),
            model,
            String.format(
                EditProjectCommand.MESSAGE_EDIT_PROJECT_SUCCESS,
                Messages.format(expectedProject)
            ),
            expectedModel
        );

        assertEquals(target.getProjectId(),
            model.getFilteredProjectList().get(index.getZeroBased()).getProjectId());
    }
}
