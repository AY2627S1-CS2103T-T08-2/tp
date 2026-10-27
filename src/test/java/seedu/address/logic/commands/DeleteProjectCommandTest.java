package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.ProjectBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteProjectCommand}.
 */
public class DeleteProjectCommandTest {

    private static final Index FIRST_PROJECT = Index.fromOneBased(1);
    private static final Index SECOND_PROJECT = Index.fromOneBased(2);
    private static final Index THIRD_PROJECT = Index.fromOneBased(3);

    private final Project firstProject = createProject("Zeta Project", 1);
    private final Project secondProject = createProject("Alpha Project", 2);
    private final Project thirdProject = createProject("Beta Project", 3);

    private final Person firstContact =
        new PersonBuilder().withName("Alex Tan").withPhone("91234567").build();
    private final Person secondContact =
        new PersonBuilder().withName("Alex Tan").withPhone("98765432").build();
    private final Person unassociatedContact =
        new PersonBuilder().withName("Mina Lee").build();

    private final ProjectMembership firstMembership =
        createMembership(firstContact, firstProject, "Design the model");
    private final ProjectMembership secondMembership =
        createMembership(secondContact, firstProject, "Review the model");
    private final ProjectMembership thirdMembership =
        createMembership(firstContact, secondProject, "Implement the parser");
    private final ProjectMembership fourthMembership =
        createMembership(secondContact, thirdProject, "Write documentation");

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();

        model.addPerson(firstContact);
        model.addPerson(secondContact);
        model.addPerson(unassociatedContact);

        model.addProject(firstProject);
        model.addProject(secondProject);
        model.addProject(thirdProject);

        model.addProjectMembership(firstMembership);
        model.addProjectMembership(secondMembership);
        model.addProjectMembership(thirdMembership);
        model.addProjectMembership(fourthMembership);
    }

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
            () -> new DeleteProjectCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
            () -> new DeleteProjectCommand(FIRST_PROJECT).execute(null));
    }

    @Test
    public void execute_firstProject_removesOnlySelectedProjectAndItsMemberships()
        throws Exception {
        CommandResult result =
            new DeleteProjectCommand(FIRST_PROJECT).execute(model);

        assertEquals(
            List.of(secondProject, thirdProject),
            model.getProjectList());

        assertMembershipsEqual(
            List.of(thirdMembership, fourthMembership),
            model.getAddressBook().getProjectMembershipList());

        assertContactsUnchanged();
        assertSuccessFeedback(result, firstProject);
    }

    @Test
    public void execute_middleProject_preservesRemainingOrderAndRoles()
        throws Exception {
        CommandResult result =
            new DeleteProjectCommand(SECOND_PROJECT).execute(model);

        assertEquals(
            List.of(firstProject, thirdProject),
            model.getProjectList());

        assertMembershipsEqual(
            List.of(firstMembership, secondMembership, fourthMembership),
            model.getAddressBook().getProjectMembershipList());

        assertContactsUnchanged();
        assertSuccessFeedback(result, secondProject);
    }

    @Test
    public void execute_lastProject_success() throws Exception {
        new DeleteProjectCommand(THIRD_PROJECT).execute(model);

        assertEquals(
            List.of(firstProject, secondProject),
            model.getProjectList());

        assertMembershipsEqual(
            List.of(firstMembership, secondMembership, thirdMembership),
            model.getAddressBook().getProjectMembershipList());

        assertContactsUnchanged();
    }

    @Test
    public void execute_projectWithoutMembers_preservesAllExistingMemberships()
        throws Exception {
        Project emptyProject =
            new ProjectBuilder().withName("Empty Project").build();
        model.addProject(emptyProject);

        new DeleteProjectCommand(Index.fromOneBased(4)).execute(model);

        assertEquals(
            List.of(firstProject, secondProject, thirdProject),
            model.getProjectList());

        assertMembershipsEqual(
            List.of(firstMembership, secondMembership,
                thirdMembership, fourthMembership),
            model.getAddressBook().getProjectMembershipList());

        assertContactsUnchanged();
    }

    @Test
    public void execute_onlyProject_leavesContactsAndNoMemberships()
        throws Exception {
        model = new ModelManager();
        model.addPerson(firstContact);
        model.addProject(firstProject);
        model.addProjectMembership(firstMembership);

        new DeleteProjectCommand(FIRST_PROJECT).execute(model);

        assertTrue(model.getProjectList().isEmpty());
        assertTrue(model.getAddressBook().getProjectMembershipList().isEmpty());

        assertEquals(
            List.of(firstContact),
            model.getAddressBook().getPersonList());
        assertEquals(
            List.of(firstContact),
            model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyProjectList_throwsCommandException() {
        model = new ModelManager();
        model.addPerson(firstContact);

        assertCommandFailure(
            new DeleteProjectCommand(FIRST_PROJECT),
            model,
            Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);

        assertEquals(
            List.of(firstContact),
            model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_indexBeyondLastProject_preservesAllDataAndContactFilter() {
        model.updateFilteredPersonList(person ->
            person.getPersonId().equals(firstContact.getPersonId()));

        assertCommandFailure(
            new DeleteProjectCommand(Index.fromOneBased(4)),
            model,
            Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);

        assertMembershipsEqual(
            List.of(firstMembership, secondMembership,
                thirdMembership, fourthMembership),
            model.getAddressBook().getProjectMembershipList());

        assertEquals(
            List.of(firstContact),
            model.getFilteredPersonList());

        assertContactsUnchanged();
    }

    @Test
    public void execute_maximumIntegerIndex_throwsCommandException() {
        assertCommandFailure(
            new DeleteProjectCommand(Index.fromOneBased(Integer.MAX_VALUE)),
            model,
            Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_contactFilterActive_usesProjectIndexAndPreservesFilter()
        throws Exception {
        model.updateFilteredPersonList(person ->
            person.getPersonId().equals(secondContact.getPersonId()));

        assertEquals(
            List.of(secondContact),
            model.getFilteredPersonList());

        new DeleteProjectCommand(SECOND_PROJECT).execute(model);

        assertEquals(
            List.of(firstProject, thirdProject),
            model.getProjectList());
        assertEquals(
            List.of(secondContact),
            model.getFilteredPersonList());

        assertContactsUnchanged();
    }

    @Test
    public void execute_noDisplayedContacts_stillDeletesProject()
        throws Exception {
        model.updateFilteredPersonList(person -> false);

        new DeleteProjectCommand(SECOND_PROJECT).execute(model);

        assertEquals(
            List.of(firstProject, thirdProject),
            model.getProjectList());

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertContactsUnchanged();
    }

    @Test
    public void execute_deletedProjectMemberFilter_reEvaluatesToEmptyList()
        throws Exception {
        model.updateFilteredPersonList(person -> model.hasProjectMembership(
            new ProjectMembership(
                person.getPersonId(), firstProject.getProjectId())));

        assertEquals(
            List.of(firstContact, secondContact),
            model.getFilteredPersonList());

        new DeleteProjectCommand(FIRST_PROJECT).execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertContactsUnchanged();
    }

    @Test
    public void execute_anotherProjectMemberFilter_preservesVisibleMembers()
        throws Exception {
        Predicate<Person> memberFilter = person -> model.hasProjectMembership(
            new ProjectMembership(
                person.getPersonId(), secondProject.getProjectId()));
        model.updateFilteredPersonList(memberFilter);

        new DeleteProjectCommand(FIRST_PROJECT).execute(model);

        assertEquals(
            List.of(firstContact),
            model.getFilteredPersonList());

        assertTrue(model.getFilteredPersonListPredicate().test(firstContact));
        assertFalse(model.getFilteredPersonListPredicate().test(secondContact));

        assertMembershipsEqual(
            List.of(thirdMembership, fourthMembership),
            model.getAddressBook().getProjectMembershipList());
    }

    @Test
    public void execute_sameDisplayedIndexTwice_deletesNextProjectAfterReindexing()
        throws Exception {
        DeleteProjectCommand command = new DeleteProjectCommand(FIRST_PROJECT);

        command.execute(model);
        command.execute(model);

        assertEquals(List.of(thirdProject), model.getProjectList());

        assertMembershipsEqual(
            List.of(fourthMembership),
            model.getAddressBook().getProjectMembershipList());

        assertContactsUnchanged();
    }

    @Test
    public void execute_deletedNameReused_doesNotRestoreOldMemberships()
        throws Exception {
        new DeleteProjectCommand(FIRST_PROJECT).execute(model);

        Project replacement = new ProjectBuilder()
            .withName(firstProject.getName().toString())
            .build();
        model.addProject(replacement);

        assertFalse(
            replacement.getProjectId().equals(firstProject.getProjectId()));
        assertTrue(model.getProjectMembers(replacement).isEmpty());

        assertEquals(
            List.of(secondProject),
            model.getPersonProjects(firstContact));
        assertEquals(
            List.of(thirdProject),
            model.getPersonProjects(secondContact));
    }

    @Test
    public void equals_comparesTargetIndices() {
        DeleteProjectCommand command = new DeleteProjectCommand(FIRST_PROJECT);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new DeleteProjectCommand(FIRST_PROJECT)));
        assertFalse(command.equals(new DeleteProjectCommand(SECOND_PROJECT)));
        assertFalse(command.equals(new DeleteCommand(FIRST_PROJECT)));
        assertFalse(command.equals(null));
    }

    private void assertContactsUnchanged() {
        List<Person> expected =
            List.of(firstContact, secondContact, unassociatedContact);

        assertEquals(expected, model.getAddressBook().getPersonList());

        assertEquals(
            expected.stream().map(Person::getPersonId).toList(),
            model.getAddressBook().getPersonList().stream()
                .map(Person::getPersonId).toList());
    }

    private void assertSuccessFeedback(
        CommandResult result, Project deletedProject) {
        String expectedMessage = String.format(
            DeleteProjectCommand.MESSAGE_DELETE_PROJECT_SUCCESS,
            Messages.format(deletedProject));

        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    private static void assertMembershipsEqual(
        List<ProjectMembership> expected, List<ProjectMembership> actual) {
        assertEquals(expected, actual);

        // Membership equality does not compare roles.
        for (int index = 0; index < expected.size(); index++) {
            assertEquals(expected.get(index).getRole(), actual.get(index).getRole());
        }
    }

    private static Project createProject(String name, int day) {
        return new ProjectBuilder()
            .withName(name)
            .withDeadline(LocalDate.of(2027, 1, day))
            .withDescription("Description for " + name)
            .build();
    }

    private static ProjectMembership createMembership(
        Person contact, Project project, String role) {
        return new ProjectMembership(
            contact.getPersonId(), project.getProjectId(), new Role(role));
    }
}
