package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;
import seedu.address.model.project.ProjectNameContainsKeywordsPredicate;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ProjectBuilder;

/** Tests project search and its interactions with displayed project indices. */
public class FindProjectCommandTest {


    private static final Index FIRST_INDEX = Index.fromOneBased(1);

    @TempDir
    public Path temporaryFolder;

    private final Project alpha = createProject("Alpha Project");
    private final Project beta = createProject("Beta Project");
    private final Project gamma = createProject("Gamma Project");
    private final List<Project> allProjects = List.of(alpha, beta, gamma);
    private final Role originalRole = new Role("Original role");

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BOB);
        allProjects.forEach(model::addProject);
        model.addProjectMembership(new ProjectMembership(
            ALICE.getPersonId(), alpha.getProjectId(), originalRole));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command("Alpha").execute(null));
    }

    @Test
    public void execute_noMatches_showsEmptyProjectResults() {
        CommandResult result = command("Missing").execute(model);

        assertTrue(model.getFilteredProjectList().isEmpty());
        assertEquals("0 project(s) listed!", result.getFeedbackToUser());
        assertEquals(CommandResult.ListViewTarget.PROJECTS, result.getListViewTarget());
    }

    @Test
    public void execute_multipleMatches_preservesInsertionOrderAndCountsProjects() {
        CommandResult result = command("GAMMA", "alpha").execute(model);

        assertEquals(List.of(alpha, gamma), model.getFilteredProjectList());
        assertEquals("2 project(s) listed!", result.getFeedbackToUser());
        assertEquals(CommandResult.ListViewTarget.PROJECTS, result.getListViewTarget());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_secondSearch_replacesPreviousSearch() {
        command("Alpha").execute(model);
        command("Beta").execute(model);

        assertEquals(List.of(beta), model.getFilteredProjectList());
    }

    @Test
    public void execute_contactFilterActive_preservesFilterAndStoredData() {
        model.updateFilteredPersonList(person -> person.getPersonId().equals(BOB.getPersonId()));
        Predicate<? super Person> contactFilter = model.getFilteredPersonListPredicate();
        AddressBook beforeSearch = new AddressBook(model.getAddressBook());

        command("Beta").execute(model);

        assertEquals(List.of(beta), model.getFilteredProjectList());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertSame(contactFilter, model.getFilteredPersonListPredicate());
        assertEquals(beforeSearch, model.getAddressBook());
        assertEquals(originalRole, model.getAddressBook().getProjectMembershipList().getFirst().getRole());
    }

    @Test
    public void execute_hiddenProject_remainsFindableAndRejectedAsDuplicate() {
        command("Beta").execute(model);
        Project duplicate = createProject("Alpha Project");

        assertEquals(alpha, model.findProject(new ProjectName("Alpha Project")).orElseThrow());
        assertThrows(CommandException.class,
            String.format(AddProjectCommand.MESSAGE_DUPLICATE_PROJECT, duplicate.getName()), () -> {
                new AddProjectCommand(duplicate).execute(model);
            });
        assertEquals(allProjects, model.getAddressBook().getProjectList());
        assertEquals(List.of(beta), model.getFilteredProjectList());
    }

    @Test
    public void execute_deleteFilteredIndex_deletesDisplayedProjectAndPreservesSearch() throws CommandException {
        command("Beta").execute(model);

        new DeleteProjectCommand(FIRST_INDEX).execute(model);

        assertEquals(List.of(alpha, gamma), model.getAddressBook().getProjectList());
        assertTrue(model.getFilteredProjectList().isEmpty());
    }

    @Test
    public void execute_indexOutsideFilteredResults_rejectsHiddenProjectIndex() {
        command("Beta").execute(model);

        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX, () -> {
            new DeleteProjectCommand(Index.fromOneBased(2)).execute(model);
        });

        assertEquals(allProjects, model.getAddressBook().getProjectList());
        assertEquals(List.of(beta), model.getFilteredProjectList());
    }

    @Test
    public void execute_editFilteredIndex_editsDisplayedProjectAndClearsSearch() throws CommandException {
        command("Beta").execute(model);
        EditProjectCommand.EditProjectDescriptor descriptor = new EditProjectCommand.EditProjectDescriptor();
        descriptor.setName(new ProjectName("Delta Project"));

        new EditProjectCommand(FIRST_INDEX, descriptor).execute(model);

        assertEquals(3, model.getFilteredProjectList().size());
        assertEquals(alpha, model.getFilteredProjectList().get(0));
        assertEquals(beta.getProjectId(), model.getFilteredProjectList().get(1).getProjectId());
        assertEquals("Delta Project", model.getFilteredProjectList().get(1).getName().toString());
        assertEquals(gamma, model.getFilteredProjectList().get(2));
    }

    @Test
    public void execute_associateFilteredIndex_associatesDisplayedProject() throws CommandException {
        command("Beta").execute(model);

        new AssociateCommand(FIRST_INDEX, FIRST_INDEX).execute(model);

        assertTrue(model.hasProjectMembership(new ProjectMembership(ALICE.getPersonId(), beta.getProjectId())));
        assertEquals(List.of(alpha, beta), model.getPersonProjects(ALICE));
    }

    @Test
    public void execute_dissociateFilteredIndex_preservesOtherMemberships() throws CommandException {
        ProjectMembership betaMembership = new ProjectMembership(ALICE.getPersonId(), beta.getProjectId());
        model.addProjectMembership(betaMembership);
        command("Beta").execute(model);

        new DissociateCommand(FIRST_INDEX, FIRST_INDEX).execute(model);

        assertFalse(model.hasProjectMembership(betaMembership));
        assertEquals(List.of(alpha), model.getPersonProjects(ALICE));
        assertEquals(originalRole, model.getAddressBook().getProjectMembershipList().getFirst().getRole());
    }

    @Test
    public void execute_setRoleFilteredIndex_updatesOnlyDisplayedProjectMembership() throws CommandException {
        ProjectMembership betaMembership = new ProjectMembership(ALICE.getPersonId(), beta.getProjectId());
        model.addProjectMembership(betaMembership);
        command("Beta").execute(model);
        Role updatedRole = new Role("Implement search");

        new SetRoleCommand(FIRST_INDEX, FIRST_INDEX, updatedRole).execute(model);

        List<ProjectMembership> memberships = model.getAddressBook().getProjectMembershipList();
        assertEquals(originalRole, memberships.get(0).getRole());
        assertEquals(betaMembership, memberships.get(1));
        assertEquals(updatedRole, memberships.get(1).getRole());
    }

    @Test
    public void execute_addProjectAfterSearch_clearsSearch() throws CommandException {
        command("Beta").execute(model);
        Project added = createProject("Delta Project");

        new AddProjectCommand(added).execute(model);

        assertEquals(List.of(alpha, beta, gamma, added), model.getFilteredProjectList());
    }

    @Test
    public void execute_listProjectsAfterSearch_restoresAllProjects() throws CommandException {
        command("Missing").execute(model);

        CommandResult result = new ListProjectsCommand().execute(model);

        assertEquals(allProjects, model.getFilteredProjectList());
        assertEquals(CommandResult.ListViewTarget.PROJECTS, result.getListViewTarget());
        assertTrue(result.getFeedbackToUser().startsWith("Listed 3 project(s):\n"));
    }

    @Test
    public void execute_listContactProjectsAfterSearch_preservesFilter() throws CommandException {
        command("Beta").execute(model);

        CommandResult result = new ListProjectsCommand(FIRST_INDEX).execute(model);

        assertEquals(List.of(beta), model.getFilteredProjectList());
        assertEquals(String.format("Projects for contact \"%s\" (1):\n", ALICE.getName())
                + "[not displayed] Alpha Project | Deadline: 2027-01-01 | Description: Test project",
            result.getFeedbackToUser());
    }

    @Test
    public void execute_listUnassociatedContactAfterSearch_preservesFilter() throws CommandException {
        command("Beta").execute(model);

        CommandResult result = new ListProjectsCommand(Index.fromOneBased(2)).execute(model);

        assertEquals(List.of(beta), model.getFilteredProjectList());
        assertEquals(String.format(ListProjectsCommand.MESSAGE_NO_PROJECTS, BOB.getName()),
            result.getFeedbackToUser());
    }

    @Test
    public void execute_listInvalidContactAfterSearch_preservesProjectFilter() {
        command("Beta").execute(model);

        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () -> {
            new ListProjectsCommand(Index.fromOneBased(3)).execute(model);
        });

        assertEquals(List.of(beta), model.getFilteredProjectList());
    }

    @Test
    public void execute_saveWhileFiltered_preservesHiddenProjects() throws Exception {
        command("Beta").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));

        storage.saveAddressBook(model.getAddressBook());

        assertEquals(allProjects, storage.readAddressBook().orElseThrow().getProjectList());
    }

    @Test
    public void execute_failedSaveAfterEdit_restoresProjectAndBothFilters() throws Exception {
        command("Beta").execute(model);
        model.updateFilteredPersonList(person -> person.getPersonId().equals(ALICE.getPersonId()));
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("failed.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("simulated save failure");
            }
        };
        Logic logic = new LogicManager(model, new StorageManager(failingStorage,
            new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        assertThrows(CommandException.class, () -> logic.execute("editproject 1 n/Delta Project"));

        assertEquals(allProjects, model.getAddressBook().getProjectList());
        assertEquals(List.of(beta), model.getFilteredProjectList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(originalRole, model.getAddressBook().getProjectMembershipList().getFirst().getRole());
    }

    @Test
    public void equals_comparesPredicates() {
        FindProjectCommand command = command("Alpha");

        assertTrue(command.equals(command));
        assertTrue(command.equals(command("Alpha")));
        assertFalse(command.equals(command("Beta")));
        assertFalse(command.equals(new ListProjectsCommand()));
        assertFalse(command.equals(null));
    }

    private FindProjectCommand command(String... keywords) {
        return new FindProjectCommand(new ProjectNameContainsKeywordsPredicate(List.of(keywords)));
    }

    private static Project createProject(String name) {
        return new ProjectBuilder().withName(name)
            .withDeadline(LocalDate.of(2027, 1, 1)).withDescription("Test project").build();
    }
}
