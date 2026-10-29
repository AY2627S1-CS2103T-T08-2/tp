package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ProjectBuilder;

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

        assertEquals(new Role("Implement parser"), model
            .getAddressBook()
            .getProjectMembershipList()
            .get(0)
            .getRole());
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

        ProjectMembership updatedMembership = model
            .getAddressBook()
            .getProjectMembershipList()
            .get(0);
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

        assertTrue(command
            .toString()
            .contains(
                "contactIndex=seedu.address.commons.core.index.Index{zeroBasedIndex=0}"));
        assertTrue(command
            .toString()
            .contains(
                "projectIndex=seedu.address.commons.core.index.Index{zeroBasedIndex=1}"));
        assertTrue(command
            .toString()
            .contains("role=Implement parser"));
    }

    @Test
    public void execute_emptyRole_clearsRoleAndRetainsMembership() throws Exception {
        Model model = modelWithRole(new Role("Implement parser"));

        CommandResult result = new SetRoleCommand(
            Index.fromOneBased(1), Index.fromOneBased(1), Role.EMPTY).execute(model);

        List<ProjectMembership> memberships = model
            .getAddressBook()
            .getProjectMembershipList();
        assertEquals(1, memberships.size());
        assertEquals(new ProjectMembership(ALICE.getPersonId(), project.getProjectId()), memberships.getFirst());
        assertEquals(Role.EMPTY, memberships
            .getFirst()
            .getRole());
        assertEquals(List.of(project), model.getPersonProjects(ALICE));
        assertEquals(String.format(SetRoleCommand.MESSAGE_CLEAR_SUCCESS, ALICE.getName(), project.getName()),
            result.getFeedbackToUser());
    }

    @Test
    public void execute_alreadyEmptyRole_remainsAssociated() throws Exception {
        Model model = modelWithRole(Role.EMPTY);
        SetRoleCommand command = new SetRoleCommand(
            Index.fromOneBased(1), Index.fromOneBased(1), Role.EMPTY);

        command.execute(model);
        command.execute(model);

        assertEquals(1, model
            .getAddressBook()
            .getProjectMembershipList()
            .size());
        assertEquals(Role.EMPTY, model
            .getAddressBook()
            .getProjectMembershipList()
            .getFirst()
            .getRole());
        assertEquals(List.of(project), model.getPersonProjects(ALICE));
    }

    @Test
    public void execute_clearFilteredMembership_preservesOtherRoleAndFilters() throws Exception {
        Model model = new ModelManager();
        Project otherProject = new ProjectBuilder()
            .withName("Other Project")
            .build();
        Role otherRole = new Role("Other task");
        ProjectMembership other = new ProjectMembership(ALICE.getPersonId(), otherProject.getProjectId(), otherRole);
        ProjectMembership target = new ProjectMembership(
            BENSON.getPersonId(), project.getProjectId(), new Role("Task"));

        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addProject(otherProject);
        model.addProject(project);
        model.addProjectMembership(other);
        model.addProjectMembership(target);
        model.updateFilteredPersonList(person -> person
            .getPersonId()
            .equals(BENSON.getPersonId()));
        model.updateFilteredProjectList(value -> value
            .getProjectId()
            .equals(project.getProjectId()));

        new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(1), Role.EMPTY).execute(model);

        List<ProjectMembership> memberships = model
            .getAddressBook()
            .getProjectMembershipList();
        assertEquals(List.of(other, target), memberships);
        assertEquals(List.of(otherRole, Role.EMPTY), memberships
            .stream()
            .map(ProjectMembership::getRole)
            .toList());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(List.of(project), model.getFilteredProjectList());
    }

    @Test
    public void execute_clearUnassociatedContact_throwsCommandException() {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addProject(project);

        assertThrows(CommandException.class,
            String.format(SetRoleCommand.MESSAGE_NOT_ASSOCIATED, ALICE.getName(), project.getName()), () -> {
                new SetRoleCommand(Index.fromOneBased(1), Index.fromOneBased(1), Role.EMPTY).execute(model);
            });

        assertTrue(model
            .getAddressBook()
            .getProjectMembershipList()
            .isEmpty());
    }

    @Test
    public void execute_clearRole_preservesMembershipAfterReload(@TempDir Path folder) throws Exception {
        Model model = modelWithRole(new Role("Implement parser"));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("addressbook.json"));
        Logic logic = new LogicManager(model,
            new StorageManager(storage, new JsonUserPrefsStorage(folder.resolve("preferences.json"))));

        logic.execute("setrole c/1 pr/1 r/");

        ReadOnlyAddressBook restored = storage
            .readAddressBook()
            .orElseThrow();
        assertEquals(1, restored
            .getProjectMembershipList()
            .size());
        assertEquals(new ProjectMembership(ALICE.getPersonId(), project.getProjectId()),
            restored
                .getProjectMembershipList()
                .getFirst());
        assertEquals(Role.EMPTY, restored
            .getProjectMembershipList()
            .getFirst()
            .getRole());
        assertEquals(List.of(ALICE), restored.getPersonList());
        assertEquals(List.of(project), restored.getProjectList());
    }

    private Model modelWithRole(Role role) {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addProject(project);
        model.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), project.getProjectId(), role));
        return model;
    }
}
