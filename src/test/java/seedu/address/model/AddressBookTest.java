package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;
import seedu.address.model.project.exceptions.DuplicateProjectException;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
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
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons, List.of());

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
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
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected =
                AddressBook.class.getCanonicalName() + String.format(
                        "{persons=%s, projects=%s}",
                        addressBook.getPersonList(),
                        addressBook.getProjectList()
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

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private final ObservableList<Project> projects = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons, Collection<Project> projects) {
            this.persons.setAll(persons);
            this.projects.setAll(projects);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }

        @Override
        public ObservableList<Project> getProjectList() {
            return projects;
        }
    }

    private Project createProject(String name) {
        return new Project(
                new ProjectName(name),
                new Deadline(LocalDate.of(2026, 11, 1)),
                Description.EMPTY);
    }
}
