package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.membership.exceptions.DuplicateProjectMembershipException;
import seedu.address.model.membership.exceptions.ProjectMembershipNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectId;
import seedu.address.model.project.ProjectName;
import seedu.address.model.project.exceptions.DuplicateProjectException;
import seedu.address.model.project.exceptions.ProjectNotFoundException;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.ProjectBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
        assertEquals(List.of(), addressBook.getProjectList());
        assertEquals(List.of(), addressBook.getProjectMembershipList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Matching contact details must be rejected even when identifiers differ.
        Person editedAlice = new PersonBuilder(ALICE).withPersonId(PersonId.generate()).build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons, List.of(), List.of());

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void resetData_withMissingMembershipReference_throwsWithoutChangingData() {
        Project project = createProject("CS2103T");
        ProjectMembership membership = addMembership(addressBook, ALICE, project);
        AddressBook original = new AddressBook(addressBook);
        AddressBookStub invalidData = new AddressBookStub(List.of(BOB), List.of(project), List.of(membership));

        assertThrows(PersonNotFoundException.class, () -> addressBook.resetData(invalidData));

        assertEquals(original, addressBook);
    }

    @Test
    public void setPersons_emptyReplacementWithMembership_throwsWithoutChangingData() {
        Project project = createProject("CS2103T");
        addMembership(addressBook, ALICE, project);
        AddressBook original = new AddressBook(addressBook);

        assertThrows(PersonNotFoundException.class, () -> addressBook.setPersons(List.of()));

        assertEquals(original, addressBook);
    }

    @Test
    public void setPersons_replacementRetainsMembershipAndRole() {
        Project project = createProject("CS2103T");
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BOB);
        addressBook.addProject(project);
        ProjectMembership membership = new ProjectMembership(ALICE.getPersonId(), project.getProjectId(),
                new Role("Lead"));
        addressBook.addProjectMembership(membership);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();

        addressBook.setPersons(List.of(editedAlice));

        assertEquals(List.of(editedAlice), addressBook.getPersonList());
        assertEquals(List.of(membership), addressBook.getProjectMembershipList());
        assertEquals(new Role("Lead"), addressBook.getProjectMembershipList().get(0).getRole());
    }

    @Test
    public void setProjects_emptyReplacementWithMembership_throwsWithoutChangingData() {
        Project project = createProject("CS2103T");
        addMembership(addressBook, ALICE, project);
        AddressBook original = new AddressBook(addressBook);

        assertThrows(ProjectNotFoundException.class, () -> addressBook.setProjects(List.of()));

        assertEquals(original, addressBook);
    }

    @Test
    public void setProjects_replacementRetainsMembershipAndRole() {
        Project project = createProject("CS2103T");
        Project unreferencedProject = createProject("Orbital");
        addressBook.addPerson(ALICE);
        addressBook.addProject(project);
        addressBook.addProject(unreferencedProject);
        ProjectMembership membership = new ProjectMembership(ALICE.getPersonId(), project.getProjectId(),
                new Role("Lead"));
        addressBook.addProjectMembership(membership);
        Project editedProject = new ProjectBuilder(project).withName("Renamed project").build();

        addressBook.setProjects(List.of(editedProject));

        assertEquals(List.of(editedProject), addressBook.getProjectList());
        assertEquals(List.of(membership), addressBook.getProjectMembershipList());
        assertEquals(new Role("Lead"), addressBook.getProjectMembershipList().get(0).getRole());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_sameNameWithDifferentDetails_returnsFalse() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertFalse(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void setPerson_sameNameContacts_preservesIdsAndMemberships() {
        Project project = createProject("CS2103T");
        Person otherAlice = new PersonBuilder(ALICE).withPersonId(PersonId.generate())
                .withAddress(VALID_ADDRESS_BOB).build();
        ProjectMembership aliceMembership = addMembership(addressBook, ALICE, project);
        addressBook.addPerson(otherAlice);
        ProjectMembership otherMembership = new ProjectMembership(otherAlice.getPersonId(), project.getProjectId());
        addressBook.addProjectMembership(otherMembership);
        Person editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();

        addressBook.setPerson(ALICE, editedAlice);

        assertEquals(List.of(editedAlice, otherAlice), addressBook.getPersonList());
        assertEquals(List.of(ALICE.getPersonId(), otherAlice.getPersonId()),
                addressBook.getPersonList().stream().map(Person::getPersonId).toList());
        assertEquals(List.of(aliceMembership, otherMembership), addressBook.getProjectMembershipList());
        assertEquals(List.of(editedAlice, otherAlice), addressBook.getProjectMembers(project));
    }

    @Test
    public void removePerson_sameNameContacts_removesOnlySelectedContactAndMemberships() {
        Project project = createProject("CS2103T");
        Person otherAlice = new PersonBuilder(ALICE).withPersonId(PersonId.generate())
                .withAddress(VALID_ADDRESS_BOB).build();
        addMembership(addressBook, ALICE, project);
        addressBook.addPerson(otherAlice);
        ProjectMembership otherMembership = new ProjectMembership(otherAlice.getPersonId(), project.getProjectId());
        addressBook.addProjectMembership(otherMembership);

        addressBook.removePerson(ALICE);

        assertEquals(List.of(otherAlice), addressBook.getPersonList());
        assertEquals(List.of(otherMembership), addressBook.getProjectMembershipList());
        assertEquals(List.of(otherAlice), addressBook.getProjectMembers(project));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected =
                AddressBook.class.getCanonicalName() + String.format(
                        "{persons=%s, projects=%s, memberships=%s}",
                        addressBook.getPersonList(),
                        addressBook.getProjectList(),
                        addressBook.getProjectMembershipList()
                );
        assertEquals(expected, addressBook.toString());
    }

    @Test
    public void addProject_newProject_success() {
        AddressBook addressBook = new AddressBook();
        Project project = createProject("CS2103T");

        assertFalse(addressBook.hasProject(project));

        addressBook.addProject(project);

        assertTrue(addressBook.hasProject(project));
        assertEquals(List.of(project), addressBook.getProjectList());
    }

    @Test
    public void addProject_duplicateName_throwsDuplicateProjectException() {
        AddressBook addressBook = new AddressBook();
        Project original = createProject("CS2103T Team");
        Project duplicate = new Project(
                new ProjectName("  cs2103t   team  "),
                new Deadline(LocalDate.of(2026, 12, 1)),
                new Description("Different details"));
        addressBook.addProject(original);

        assertThrows(DuplicateProjectException.class, () -> addressBook.addProject(duplicate));

        assertEquals(List.of(original), addressBook.getProjectList());
    }

    @Test
    public void constructor_withProjects_copiesProjectsIndependently() {
        AddressBook original = new AddressBook();
        Project project = createProject("CS2103T");
        original.addProject(project);

        AddressBook copy = new AddressBook(original);

        assertEquals(List.of(project), copy.getProjectList());
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());

        // Changing the original collection must not change the copy.
        original.addProject(createProject("Orbital"));

        assertEquals(List.of(project), copy.getProjectList());
    }

    @Test
    public void resetData_withProjects_replacesExistingProjects() {
        AddressBook addressBook = new AddressBook();
        addressBook.addProject(createProject("Old Project"));

        AddressBook replacement = new AddressBook();
        Project project = createProject("New Project");
        replacement.addProject(project);

        addressBook.resetData(replacement);

        assertEquals(List.of(project), addressBook.getProjectList());
    }

    @Test
    public void resetData_emptyAddressBook_clearsProjects() {
        AddressBook addressBook = new AddressBook();
        addressBook.addProject(createProject("CS2103T"));

        addressBook.resetData(new AddressBook());

        assertTrue(addressBook.getProjectList().isEmpty());
    }

    @Test
    public void equals_differentProjects_returnsFalse() {
        AddressBook first = new AddressBook();
        AddressBook second = new AddressBook();
        first.addProject(createProject("CS2103T"));
        second.addProject(createProject("Orbital"));

        assertFalse(first.equals(second));
    }

    @Test
    public void getProjectList_modifyList_throwsUnsupportedOperationException() {
        AddressBook addressBook = new AddressBook();
        addressBook.addProject(createProject("CS2103T"));

        assertThrows(UnsupportedOperationException.class, () -> addressBook.getProjectList().clear());
    }

    @Test
    public void hasProjectMembership_nullMembership_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasProjectMembership(null));
    }

    @Test
    public void addProjectMembership_validMembership_success() {
        Project project = createProject("CS2103T");
        ProjectMembership membership = addMembership(addressBook, ALICE, project);

        assertTrue(addressBook.hasProjectMembership(membership));
        assertEquals(List.of(membership), addressBook.getProjectMembershipList());
    }

    @Test
    public void setProjectMembership_nullMembership_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.setProjectMembership(null));
    }

    @Test
    public void setProjectMembership_existingMembership_replacesRole() {
        Project project = createProject("CS2103T");
        ProjectMembership membership = addMembership(addressBook, ALICE, project);
        ProjectMembership updatedMembership = membership.withRole(new Role("Write tests"));

        addressBook.setProjectMembership(updatedMembership);

        assertEquals(List.of(updatedMembership), addressBook.getProjectMembershipList());
    }

    @Test
    public void setProjectMembership_unknownPerson_throwsPersonNotFoundException() {
        Project project = createProject("CS2103T");
        addressBook.addProject(project);
        ProjectMembership membership = new ProjectMembership(PersonId.generate(), project.getProjectId(),
                new Role("Write tests"));

        assertThrows(PersonNotFoundException.class, () -> addressBook.setProjectMembership(membership));
    }

    @Test
    public void setProjectMembership_unknownProject_throwsProjectNotFoundException() {
        addressBook.addPerson(ALICE);
        ProjectMembership membership = new ProjectMembership(ALICE.getPersonId(), ProjectId.generate(),
                new Role("Write tests"));

        assertThrows(ProjectNotFoundException.class, () -> addressBook.setProjectMembership(membership));
    }

    @Test
    public void setProjectMembership_missingMembership_throwsProjectMembershipNotFoundException() {
        Project project = createProject("CS2103T");
        addMembership(addressBook, ALICE, project);
        Project otherProject = createProject("Orbital");
        addressBook.addProject(otherProject);
        ProjectMembership membership = new ProjectMembership(ALICE.getPersonId(), otherProject.getProjectId(),
                new Role("Write tests"));

        assertThrows(ProjectMembershipNotFoundException.class, () -> addressBook.setProjectMembership(membership));
    }

    @Test
    public void getProjectMembers_memberships_returnMembersInAddressBookOrder() {
        Project project = createProject("CS2103T");
        Project otherProject = createProject("Orbital");
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BOB);
        addressBook.addProject(project);
        addressBook.addProject(otherProject);
        addressBook.addProjectMembership(new ProjectMembership(BOB.getPersonId(), project.getProjectId()));
        addressBook.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), otherProject.getProjectId()));
        addressBook.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), project.getProjectId()));

        assertEquals(List.of(ALICE, BOB), addressBook.getProjectMembers(project));
    }

    @Test
    public void getProjectMembers_noMemberships_returnsEmptyList() {
        Project project = createProject("CS2103T");
        addressBook.addProject(project);

        assertTrue(addressBook.getProjectMembers(project).isEmpty());
    }

    @Test
    public void getProjectMembers_unknownProject_throwsProjectNotFoundException() {
        assertThrows(ProjectNotFoundException.class, () -> addressBook.getProjectMembers(createProject("Unknown")));
    }

    @Test
    public void getProjectMembers_nullProject_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.getProjectMembers(null));
    }

    @Test
    public void getPersonProjects_memberships_returnsProjectsInAddressBookOrder() {
        Project first = createProject("CS2103T");
        Project second = createProject("Orbital");
        Project unrelated = createProject("Other project");
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BOB);
        addressBook.addProject(first);
        addressBook.addProject(second);
        addressBook.addProject(unrelated);
        addressBook.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), second.getProjectId()));
        addressBook.addProjectMembership(new ProjectMembership(BOB.getPersonId(), unrelated.getProjectId()));
        addressBook.addProjectMembership(new ProjectMembership(ALICE.getPersonId(), first.getProjectId()));
        AddressBook original = new AddressBook(addressBook);

        assertEquals(List.of(first, second), addressBook.getPersonProjects(ALICE));
        assertEquals(original, addressBook);
    }

    @Test
    public void getPersonProjects_noMemberships_returnsEmptyList() {
        Project project = createProject("CS2103T");
        addMembership(addressBook, BOB, project);
        addressBook.addPerson(ALICE);

        assertTrue(addressBook.getPersonProjects(ALICE).isEmpty());
    }

    @Test
    public void getPersonProjects_sameNameContacts_usesPersonId() {
        Project project = createProject("CS2103T");
        Project otherProject = createProject("Orbital");
        Person otherAlice = new PersonBuilder(ALICE).withPersonId(PersonId.generate())
                .withAddress(VALID_ADDRESS_BOB).build();
        addMembership(addressBook, ALICE, project);
        addressBook.addPerson(otherAlice);
        addressBook.addProject(otherProject);
        addressBook.addProjectMembership(new ProjectMembership(otherAlice.getPersonId(), otherProject.getProjectId()));

        assertEquals(List.of(project), addressBook.getPersonProjects(ALICE));
        assertEquals(List.of(otherProject), addressBook.getPersonProjects(otherAlice));
    }

    @Test
    public void getPersonProjects_editedContactAndProject_returnsCurrentProject() {
        Project project = createProject("CS2103T");
        addMembership(addressBook, ALICE, project);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        Project editedProject = new ProjectBuilder(project).withName("Renamed project").build();
        addressBook.setPerson(ALICE, editedAlice);
        addressBook.setProject(project, editedProject);

        assertEquals(List.of(editedProject), addressBook.getPersonProjects(editedAlice));
        assertEquals(List.of(editedProject), addressBook.getPersonProjects(ALICE));
    }

    @Test
    public void getPersonProjects_sameDetailsWithUnknownId_throwsPersonNotFoundException() {
        Project project = createProject("CS2103T");
        addMembership(addressBook, ALICE, project);
        Person unknownAlice = new PersonBuilder(ALICE).withPersonId(PersonId.generate()).build();

        assertThrows(PersonNotFoundException.class, () -> addressBook.getPersonProjects(unknownAlice));
    }

    @Test
    public void getPersonProjects_unknownPerson_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> addressBook.getPersonProjects(ALICE));
    }

    @Test
    public void getPersonProjects_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.getPersonProjects(null));
    }

    @Test
    public void addProjectMembership_unknownPerson_throwsPersonNotFoundException() {
        Project project = createProject("CS2103T");
        addressBook.addProject(project);
        ProjectMembership membership = new ProjectMembership(PersonId.generate(), project.getProjectId());

        assertThrows(PersonNotFoundException.class, () -> addressBook.addProjectMembership(membership));
        assertTrue(addressBook.getProjectMembershipList().isEmpty());
    }

    @Test
    public void addProjectMembership_unknownProject_throwsProjectNotFoundException() {
        addressBook.addPerson(ALICE);
        ProjectMembership membership = new ProjectMembership(ALICE.getPersonId(), ProjectId.generate());

        assertThrows(ProjectNotFoundException.class, () -> addressBook.addProjectMembership(membership));
        assertTrue(addressBook.getProjectMembershipList().isEmpty());
    }

    @Test
    public void addProjectMembership_duplicateMembership_throwsDuplicateProjectMembershipException() {
        Project project = createProject("CS2103T");
        ProjectMembership membership = addMembership(addressBook, ALICE, project);

        assertThrows(DuplicateProjectMembershipException.class, () -> addressBook.addProjectMembership(membership));
        assertEquals(List.of(membership), addressBook.getProjectMembershipList());
    }

    @Test
    public void removeProjectMembership_existingMembership_removesMembership() {
        Project project = createProject("CS2103T");
        ProjectMembership membership = addMembership(addressBook, ALICE, project);

        addressBook.removeProjectMembership(membership);

        assertTrue(addressBook.getProjectMembershipList().isEmpty());
    }

    @Test
    public void removeProjectMembership_missingMembership_throwsProjectMembershipNotFoundException() {
        ProjectMembership membership = new ProjectMembership(ALICE.getPersonId(), ProjectId.generate());

        assertThrows(ProjectMembershipNotFoundException.class, () -> addressBook.removeProjectMembership(membership));
    }

    @Test
    public void removePerson_personWithMemberships_removesOnlyTheirMemberships() {
        Project project = createProject("CS2103T");
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BOB);
        addressBook.addProject(project);
        ProjectMembership aliceMembership = new ProjectMembership(ALICE.getPersonId(), project.getProjectId());
        ProjectMembership bobMembership = new ProjectMembership(BOB.getPersonId(), project.getProjectId());
        addressBook.addProjectMembership(aliceMembership);
        addressBook.addProjectMembership(bobMembership);

        addressBook.removePerson(ALICE);

        assertEquals(List.of(bobMembership), addressBook.getProjectMembershipList());
    }

    @Test
    public void removeProject_projectWithMemberships_removesOnlyItsMemberships() {
        Project project = createProject("CS2103T");
        Project otherProject = createProject("Orbital");
        addressBook.addPerson(ALICE);
        addressBook.addProject(project);
        addressBook.addProject(otherProject);
        ProjectMembership membership = new ProjectMembership(ALICE.getPersonId(), project.getProjectId());
        ProjectMembership otherMembership = new ProjectMembership(ALICE.getPersonId(), otherProject.getProjectId());
        addressBook.addProjectMembership(membership);
        addressBook.addProjectMembership(otherMembership);

        addressBook.removeProject(project);

        assertEquals(List.of(otherMembership), addressBook.getProjectMembershipList());
    }

    @Test
    public void constructor_withMemberships_copiesMembershipsIndependently() {
        AddressBook original = new AddressBook();
        Project project = createProject("CS2103T");
        ProjectMembership membership = addMembership(original, ALICE, project);

        AddressBook copy = new AddressBook(original);
        original.removeProjectMembership(membership);

        assertEquals(List.of(membership), copy.getProjectMembershipList());
    }

    @Test
    public void resetData_withMemberships_replacesExistingMemberships() {
        Project oldProject = createProject("Old Project");
        addMembership(addressBook, ALICE, oldProject);
        AddressBook replacement = new AddressBook();
        Project newProject = createProject("New Project");
        ProjectMembership replacementMembership = addMembership(replacement, BOB, newProject);

        addressBook.resetData(replacement);

        assertEquals(List.of(replacementMembership), addressBook.getProjectMembershipList());
    }

    @Test
    public void equals_differentMemberships_returnsFalse() {
        AddressBook first = new AddressBook();
        Project project = createProject("CS2103T");
        addMembership(first, ALICE, project);
        AddressBook second = new AddressBook();
        second.addPerson(ALICE);
        second.addProject(project);

        assertFalse(first.equals(second));
    }

    @Test
    public void getProjectMembershipList_modifyList_throwsUnsupportedOperationException() {
        Project project = createProject("CS2103T");
        addMembership(addressBook, ALICE, project);

        assertThrows(UnsupportedOperationException.class, () -> addressBook.getProjectMembershipList().clear());
    }

    @Test
    public void setPerson_personWithMemberships_preservesMemberships() {
        Project project = createProject("CS2103T");
        ProjectMembership membership = addMembership(addressBook, ALICE, project);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();

        addressBook.setPerson(ALICE, editedAlice);

        assertEquals(List.of(membership), addressBook.getProjectMembershipList());
    }

    @Test
    public void setProject_projectWithMemberships_preservesMemberships() {
        Project project = createProject("CS2103T");
        ProjectMembership membership = addMembership(addressBook, ALICE, project);
        Project editedProject = new ProjectBuilder(project)
            .withDeadline(LocalDate.of(2026, 12, 1)).build();

        addressBook.setProject(project, editedProject);

        assertEquals(List.of(membership), addressBook.getProjectMembershipList());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private final ObservableList<Project> projects = FXCollections.observableArrayList();
        private final ObservableList<ProjectMembership> memberships = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons, Collection<Project> projects,
                Collection<ProjectMembership> memberships) {
            this.persons.setAll(persons);
            this.projects.setAll(projects);
            this.memberships.setAll(memberships);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }

        @Override
        public ObservableList<Project> getProjectList() {
            return projects;
        }

        @Override
        public ObservableList<ProjectMembership> getProjectMembershipList() {
            return memberships;
        }
    }

    private Project createProject(String name) {
        return new Project(
                new ProjectName(name),
                new Deadline(LocalDate.of(2026, 11, 1)),
                Description.EMPTY);
    }

    private ProjectMembership addMembership(AddressBook addressBook, Person person, Project project) {
        addressBook.addPerson(person);
        addressBook.addProject(project);
        ProjectMembership membership = new ProjectMembership(person.getPersonId(), project.getProjectId());
        addressBook.addProjectMembership(membership);
        return membership;
    }
}
