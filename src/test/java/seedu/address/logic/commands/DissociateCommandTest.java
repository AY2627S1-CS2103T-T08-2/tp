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
 * Tests contact-project dissociation and its interaction with the model.
 */
public class DissociateCommandTest {

    private static final Index INDEX_FIRST_PROJECT = Index.fromOneBased(1);
    private static final Index INDEX_SECOND_PROJECT = Index.fromOneBased(2);

    private final Project firstProject = new ProjectBuilder().withName("CS2103T Team Project")
            .withDeadline(LocalDate.of(2026, 11, 13)).build();
    private final Project secondProject = new ProjectBuilder().withName("Orbital")
            .withDeadline(LocalDate.of(2027, 7, 1)).build();
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
        assertThrows(NullPointerException.class, () -> new DissociateCommand(null, INDEX_FIRST_PROJECT));
        assertThrows(NullPointerException.class, () -> new DissociateCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () -> new DissociateCommand(null, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        DissociateCommand command = new DissociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_validIndicesUnfilteredList_success() {
        ProjectMembership membership = new ProjectMembership(BENSON.getPersonId(), secondProject.getProjectId());
        model.addProjectMembership(membership);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.removeProjectMembership(membership);
        DissociateCommand command = new DissociateCommand(INDEX_SECOND_PERSON, INDEX_SECOND_PROJECT);

        assertCommandSuccess(command, model,
                String.format(DissociateCommand.MESSAGE_SUCCESS, BENSON.getName(), secondProject.getName()),
                expectedModel);
        assertTrue(model.getAddressBook().getProjectMembershipList().isEmpty());
    }

    @Test
    public void execute_validIndicesFilteredList_success() {
        ProjectMembership aliceMembership = new ProjectMembership(ALICE.getPersonId(), firstProject.getProjectId());
        ProjectMembership bensonMembership = new ProjectMembership(BENSON.getPersonId(), firstProject.getProjectId());
        model.addProjectMembership(aliceMembership);
        model.addProjectMembership(bensonMembership);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.removeProjectMembership(bensonMembership);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        DissociateCommand command = new DissociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertCommandSuccess(command, model,
                String.format(DissociateCommand.MESSAGE_SUCCESS, BENSON.getName(), firstProject.getName()),
                expectedModel);
        assertEquals(List.of(aliceMembership), model.getAddressBook().getProjectMembershipList());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_entitiesInOtherMemberships_removesOnlySelectedPair() {
        ProjectMembership selectedMembership = new ProjectMembership(ALICE.getPersonId(), firstProject.getProjectId());
        ProjectMembership sameContactMembership =
                new ProjectMembership(ALICE.getPersonId(), secondProject.getProjectId());
        ProjectMembership sameProjectMembership =
                new ProjectMembership(BENSON.getPersonId(), firstProject.getProjectId());
        model.addProjectMembership(selectedMembership);
        model.addProjectMembership(sameContactMembership);
        model.addProjectMembership(sameProjectMembership);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.removeProjectMembership(selectedMembership);
        DissociateCommand command = new DissociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertCommandSuccess(command, model,
                String.format(DissociateCommand.MESSAGE_SUCCESS, ALICE.getName(), firstProject.getName()),
                expectedModel);
        assertFalse(model.hasProjectMembership(selectedMembership));
        assertEquals(List.of(sameContactMembership, sameProjectMembership),
                model.getAddressBook().getProjectMembershipList());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
        assertEquals(List.of(firstProject, secondProject), model.getProjectList());
    }

    @Test
    public void execute_projectListInsertionOrder_success() {
        Project zetaProject = new ProjectBuilder().withName("Zeta").build();
        Project alphaProject = new ProjectBuilder().withName("Alpha").build();
        model = new ModelManager();
        model.addPerson(ALICE);
        model.addProject(zetaProject);
        model.addProject(alphaProject);
        ProjectMembership zetaMembership = new ProjectMembership(ALICE.getPersonId(), zetaProject.getProjectId());
        ProjectMembership alphaMembership = new ProjectMembership(ALICE.getPersonId(), alphaProject.getProjectId());
        model.addProjectMembership(zetaMembership);
        model.addProjectMembership(alphaMembership);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.removeProjectMembership(alphaMembership);
        DissociateCommand command = new DissociateCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PROJECT);

        assertCommandSuccess(command, model,
                String.format(DissociateCommand.MESSAGE_SUCCESS, ALICE.getName(), alphaProject.getName()),
                expectedModel);
        assertEquals(List.of(zetaMembership), model.getAddressBook().getProjectMembershipList());
    }

    @Test
    public void execute_invalidContactIndexUnfilteredList_throwsCommandException() {
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), firstProject.getProjectId()));
        Index invalidContactIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DissociateCommand command = new DissociateCommand(invalidContactIndex, INDEX_FIRST_PROJECT);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidContactIndexFilteredList_throwsCommandException() {
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), firstProject.getProjectId()));
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());
        DissociateCommand command = new DissociateCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PROJECT);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidProjectIndex_throwsCommandException() {
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), firstProject.getProjectId()));
        Index invalidProjectIndex = Index.fromOneBased(model.getProjectList().size() + 1);
        DissociateCommand command = new DissociateCommand(INDEX_FIRST_PERSON, invalidProjectIndex);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyContactList_throwsCommandException() {
        model.deletePerson(ALICE);
        model.deletePerson(BENSON);
        DissociateCommand command = new DissociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyProjectList_throwsCommandException() {
        model.deleteProject(firstProject);
        model.deleteProject(secondProject);
        DissociateCommand command = new DissociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_nonexistentMembership_throwsCommandException() {
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), firstProject.getProjectId()));
        model.addProjectMembership(new ProjectMembership(BENSON.getPersonId(), secondProject.getProjectId()));
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        DissociateCommand command = new DissociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertCommandFailure(command, model,
                String.format(DissociateCommand.MESSAGE_NOT_ASSOCIATED, BENSON.getName(), firstProject.getName()));
    }

    @Test
    public void equals() {
        DissociateCommand command = new DissociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new DissociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertFalse(command.equals(new AssociateCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PROJECT)));
        assertFalse(command.equals(new DissociateCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PROJECT)));
        assertFalse(command.equals(new DissociateCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PROJECT)));
    }

    @Test
    public void toStringMethod() {
        DissociateCommand command = new DissociateCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PROJECT);
        String expected = DissociateCommand.class.getCanonicalName()
                + "{contactIndex=" + INDEX_FIRST_PERSON + ", projectIndex=" + INDEX_SECOND_PROJECT + "}";

        assertEquals(expected, command.toString());
    }
}
