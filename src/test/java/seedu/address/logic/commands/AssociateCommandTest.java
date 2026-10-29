package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.project.Project;
import seedu.address.testutil.ProjectBuilder;

/**
 * Tests contact-project association and its interaction with the model.
 */
public class AssociateCommandTest {

    private static final Index INDEX_FIRST_PROJECT = Index.fromOneBased(1);
    private static final Index INDEX_SECOND_PROJECT = Index.fromOneBased(2);

    private final Project firstProject = new ProjectBuilder()
        .withName("CS2103T Team Project")
        .withDeadline(LocalDate.of(2026, 11, 13))
        .build();
    private final Project secondProject = new ProjectBuilder()
        .withName("Orbital")
        .withDeadline(LocalDate.of(2027, 7, 1))
        .build();
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addProject(firstProject);
        model.addProject(secondProject);
    }

    @Test
    public void constructor_nullIndices_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AssociateCommand(null, INDEX_FIRST_PROJECT));
        assertThrows(NullPointerException.class, () -> new AssociateCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () -> new AssociateCommand(null, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        AssociateCommand command = new AssociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_validIndicesUnfilteredList_success() {
        AssociateCommand command = new AssociateCommand(INDEX_SECOND_PERSON, INDEX_SECOND_PROJECT);
        ProjectMembership membership = new ProjectMembership(BENSON.getPersonId(), secondProject.getProjectId());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addProjectMembership(membership);

        assertCommandSuccess(command, model,
            String.format(AssociateCommand.MESSAGE_SUCCESS, BENSON.getName(), secondProject.getName()),
            expectedModel);
        assertEquals(List.of(membership), model
            .getAddressBook()
            .getProjectMembershipList());
    }

    @Test
    public void execute_validIndicesFilteredList_success() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        AssociateCommand command = new AssociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);
        ProjectMembership membership = new ProjectMembership(BENSON.getPersonId(), firstProject.getProjectId());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addProjectMembership(membership);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        assertCommandSuccess(command, model,
            String.format(AssociateCommand.MESSAGE_SUCCESS, BENSON.getName(), firstProject.getName()),
            expectedModel);
        assertEquals(List.of(membership), model
            .getAddressBook()
            .getProjectMembershipList());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidContactIndexUnfilteredList_throwsCommandException() {
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), firstProject.getProjectId()));
        Index invalidContactIndex = Index.fromOneBased(model
            .getFilteredPersonList()
            .size() + 1);
        AssociateCommand command = new AssociateCommand(invalidContactIndex, INDEX_FIRST_PROJECT);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidContactIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model
            .getAddressBook()
            .getPersonList()
            .size());
        AssociateCommand command = new AssociateCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PROJECT);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidProjectIndex_throwsCommandException() {
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), firstProject.getProjectId()));
        Index invalidProjectIndex = Index.fromOneBased(model
            .getFilteredProjectList()
            .size() + 1);
        AssociateCommand command = new AssociateCommand(INDEX_FIRST_PERSON, invalidProjectIndex);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyContactList_throwsCommandException() {
        model.deletePerson(ALICE);
        model.deletePerson(BENSON);
        AssociateCommand command = new AssociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyProjectList_throwsCommandException() {
        model.deleteProject(firstProject);
        model.deleteProject(secondProject);
        AssociateCommand command = new AssociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_duplicateMembership_throwsCommandException() {
        ProjectMembership membership = new ProjectMembership(BENSON.getPersonId(), firstProject.getProjectId());
        model.addProjectMembership(membership);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        AssociateCommand command = new AssociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertCommandFailure(command, model,
            String.format(AssociateCommand.MESSAGE_DUPLICATE_ASSOCIATION,
                BENSON.getName(), firstProject.getName()));
    }

    @Test
    public void execute_entitiesInOtherMemberships_success() {
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), firstProject.getProjectId()));
        model.addProjectMembership(new ProjectMembership(BENSON.getPersonId(), secondProject.getProjectId()));
        ProjectMembership membership = new ProjectMembership(ALICE.getPersonId(), secondProject.getProjectId());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addProjectMembership(membership);
        AssociateCommand command = new AssociateCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PROJECT);

        assertCommandSuccess(command, model,
            String.format(AssociateCommand.MESSAGE_SUCCESS, ALICE.getName(), secondProject.getName()),
            expectedModel);
    }

    @Test
    public void equals() {
        AssociateCommand command = new AssociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new AssociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertFalse(command.equals(new DeleteCommand(INDEX_FIRST_PERSON)));
        assertFalse(command.equals(new AssociateCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PROJECT)));
        assertFalse(command.equals(new AssociateCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PROJECT)));
    }

    @Test
    public void toStringMethod() {
        AssociateCommand command = new AssociateCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PROJECT);
        String expected = AssociateCommand.class.getCanonicalName()
            + "{contactIndex=" + INDEX_FIRST_PERSON + ", projectIndex=" + INDEX_SECOND_PROJECT + "}";

        assertEquals(expected, command.toString());
    }
}
