package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectId;
import seedu.address.model.project.exceptions.ProjectNotFoundException;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.ProjectBuilder;

/** Tests aggregate membership invariants, including successful and rejected mutations. */
public class MembershipInvariantTest {
    private static final List<Person> PERSONS = IntStream.range(0, 3)
            .mapToObj(index -> new PersonBuilder().withName("Shared Name").withPhone("123" + index)
                    .withPersonId(PersonId.fromString("00000000-0000-0000-0000-00000000000" + index)).build())
            .toList();
    private static final List<Project> PROJECTS = IntStream.range(0, 3)
            .mapToObj(index -> new ProjectBuilder().withName("Project " + index)
                    .withProjectId(ProjectId.fromString("00000000-0000-0000-0000-00000000000" + index)).build())
            .toList();

    @TestFactory
    public Stream<DynamicTest> graph_allAssociationPatterns_preserveLookupCopyEditAndDeletionInvariants() {
        return IntStream.range(0, 512).mapToObj(mask -> dynamicTest("aggregate graph " + mask, () -> {
            AddressBook original = book(mask);
            List<ProjectMembership> memberships = List.copyOf(original.getProjectMembershipList());
            for (int index = 0; index < 3; index++) {
                Person person = PERSONS.get(index);
                Project project = PROJECTS.get(index);
                assertEquals(PROJECTS.stream().filter(candidate -> memberships.stream().anyMatch(pair ->
                        pair.getPersonId().equals(person.getPersonId())
                                && pair.getProjectId().equals(candidate.getProjectId()))).toList(),
                        original.getPersonProjects(person));
                assertEquals(PERSONS.stream().filter(candidate -> memberships.stream().anyMatch(pair ->
                        pair.getProjectId().equals(project.getProjectId())
                                && pair.getPersonId().equals(candidate.getPersonId()))).toList(),
                        original.getProjectMembers(project));

                AddressBook edited = new AddressBook(original);
                edited.setPerson(person, new PersonBuilder(person).withName("Renamed Contact").build());
                edited.setProject(project, new ProjectBuilder(project).withName("Renamed Project").build());
                assertMemberships(memberships, edited);

                AddressBook personRemoved = new AddressBook(original);
                personRemoved.removePerson(person);
                assertMemberships(memberships.stream()
                        .filter(pair -> !pair.getPersonId().equals(person.getPersonId())).toList(), personRemoved);
                AddressBook projectRemoved = new AddressBook(original);
                projectRemoved.removeProject(project);
                assertMemberships(memberships.stream()
                        .filter(pair -> !pair.getProjectId().equals(project.getProjectId())).toList(), projectRemoved);
            }
            AddressBook reset = book(511);
            reset.resetData(original);
            assertMemberships(memberships, reset);
            reset.setProjectMemberships(reset.getProjectMembershipList());
            assertMemberships(memberships, reset);
            reset.resetData(new AddressBook());
            assertTrue(reset.getPersonList().isEmpty());
            assertTrue(reset.getProjectList().isEmpty());
            assertTrue(reset.getProjectMembershipList().isEmpty());
            assertMemberships(memberships, original);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> unknownReferences_addSetAndBulkReplacement_rejectWithoutMutation() {
        return IntStream.range(1, 4).boxed().flatMap(mask -> IntStream.range(0, 3).boxed()
                .flatMap(operation -> IntStream.range(0, 3).mapToObj(position -> dynamicTest(
                        "unknown references " + mask + " operation " + operation + " position " + position, () -> {
                            AddressBook book = book(511);
                            List<ProjectMembership> before = List.copyOf(book.getProjectMembershipList());
                            PersonId personId = (mask & 1) == 0 ? PERSONS.get(0).getPersonId()
                                    : PersonId.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
                            ProjectId projectId = (mask & 2) == 0 ? PROJECTS.get(0).getProjectId()
                                    : ProjectId.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
                            ProjectMembership invalid = new ProjectMembership(personId, projectId);
                            Class<? extends RuntimeException> exception = (mask & 1) == 0
                                    ? ProjectNotFoundException.class : PersonNotFoundException.class;
                            assertThrows(exception, () -> {
                                if (operation == 0) {
                                    book.addProjectMembership(invalid);
                                } else if (operation == 1) {
                                    book.setProjectMembership(invalid);
                                } else {
                                    List<ProjectMembership> replacement = new ArrayList<>(before.subList(0, 2));
                                    replacement.add(position, invalid);
                                    book.setProjectMemberships(replacement);
                                }
                            });
                            assertMemberships(before, book);
                            assertEquals(PERSONS, book.getPersonList());
                            assertEquals(PROJECTS, book.getProjectList());
                        }))));
    }

    @TestFactory
    public Stream<DynamicTest> removePerson_equalDetailsWithDifferentId_cleansActualRemovedPersonsMemberships() {
        return IntStream.range(0, 3).mapToObj(index -> dynamicTest("equal deletion key position " + index, () -> {
            AddressBook book = book(511);
            Person stored = PERSONS.get(index);
            Person key = new PersonBuilder(stored)
                    .withPersonId(PersonId.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")).build();
            List<ProjectMembership> expected = book.getProjectMembershipList().stream()
                    .filter(pair -> !pair.getPersonId().equals(stored.getPersonId())).toList();
            book.removePerson(key);
            assertEquals(2, book.getPersonList().size());
            assertMemberships(expected, book);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> bulkEntityReplacement_removingReferencedEntities_leavesNoDanglingMemberships() {
        return IntStream.range(0, 2).boxed().flatMap(kind -> IntStream.range(0, 3)
                .mapToObj(retained -> dynamicTest("bulk replacement kind " + kind + " retained " + retained, () -> {
                    AddressBook book = book(511);
                    // Either rejection or successful cleanup is acceptable; dangling references are never valid.
                    try {
                        if (kind == 0) {
                            book.setPersons(PERSONS.subList(0, retained));
                        } else {
                            book.setProjects(PROJECTS.subList(0, retained));
                        }
                    } catch (IllegalArgumentException exception) {
                        assertEquals(PERSONS, book.getPersonList());
                        assertEquals(PROJECTS, book.getProjectList());
                    }
                    assertReferencesExist(book);
                })));
    }

    @Test
    public void modelManager_membershipOperations_preserveRolesAndDelegateCleanup() {
        ModelManager model = new ModelManager(book(0), new UserPrefs());
        ProjectMembership pair = new ProjectMembership(PERSONS.get(0).getPersonId(), PROJECTS.get(0).getProjectId());
        model.addProjectMembership(pair);
        assertTrue(model.hasProjectMembership(pair));
        model.setProjectMembership(pair.withRole(new Role("Changed")));
        assertEquals(new Role("Changed"), model.getAddressBook().getProjectMembershipList().get(0).getRole());
        model.removeProjectMembership(pair);
        assertTrue(model.getAddressBook().getProjectMembershipList().isEmpty());
        model.addProjectMembership(pair);
        model.deletePerson(PERSONS.get(0));
        assertTrue(model.getAddressBook().getProjectMembershipList().isEmpty());
    }

    private static AddressBook book(int mask) {
        AddressBook book = new AddressBook();
        PERSONS.forEach(book::addPerson);
        PROJECTS.forEach(book::addProject);
        List<ProjectMembership> memberships = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            if ((mask & (1 << index)) != 0) {
                memberships.add(new ProjectMembership(PERSONS.get(index / 3).getPersonId(),
                        PROJECTS.get(index % 3).getProjectId(), new Role("Role " + index)));
            }
        }
        // Reverse insertion order so lookup-order assertions cannot pass by following membership order.
        Collections.reverse(memberships);
        book.setProjectMemberships(memberships);
        return book;
    }

    private static void assertMemberships(List<ProjectMembership> expected, AddressBook actual) {
        assertEquals(expected, actual.getProjectMembershipList());
        assertEquals(expected.stream().map(ProjectMembership::getRole).toList(),
                actual.getProjectMembershipList().stream().map(ProjectMembership::getRole).toList());
        assertReferencesExist(actual);
    }

    private static void assertReferencesExist(AddressBook book) {
        for (ProjectMembership membership : book.getProjectMembershipList()) {
            assertTrue(book.getPersonList().stream()
                    .anyMatch(person -> person.getPersonId().equals(membership.getPersonId())), "Dangling person ID");
            assertTrue(book.getProjectList().stream()
                    .anyMatch(project -> project.getProjectId().equals(membership.getProjectId())),
                    "Dangling project ID");
        }
    }
}
