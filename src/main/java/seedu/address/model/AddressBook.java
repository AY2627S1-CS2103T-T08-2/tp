package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectContact;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final List<Project> projects = new ArrayList<>();
    private final List<ProjectContact> projectContacts = new ArrayList<>();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Replaces all stored projects.
     *
     * @param projects projects to store
     */
    public void setProjects(List<Project> projects) {
        this.projects.clear();
        this.projects.addAll(projects);
    }

    /**
     * Replaces all stored project-contact associations.
     *
     * @param links associations to store
     */
    public void setProjectContacts(List<ProjectContact> links) {
        this.projectContacts.clear();
        this.projectContacts.addAll(links);
    }

    /**
     * Returns an immutable list of projects ordered by project ID.
     *
     * @return projects ordered by ID
     */
    @Override
    public List<Project> getProjectList() {
        return projects.stream().sorted(Comparator.comparingInt(Project::getId))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns an unmodifiable view of all project-contact associations.
     *
     * @return stored project-contact associations
     */
    @Override
    public List<ProjectContact> getProjectContacts() {
        return Collections.unmodifiableList(projectContacts);
    }

    /**
     * Returns the contacts assigned to the specified project.
     *
     * @param projectId identifier of the project to look up
     * @return matching contacts, or an empty list if there are no matching associations
     */
    public List<Person> getProjectContacts(int projectId) {
        return projectContacts.stream().filter(link -> link.getProjectId() == projectId)
                .map(ProjectContact::getContactName)
                .map(name -> persons.asUnmodifiableObservableList().stream()
                        .filter(person -> person.getName().fullName.equals(name)).findFirst().orElse(null))
                .filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns projects assigned to a contact.
     *
     * @param contactName unique name of the contact to look up
     * @return matching projects, or an empty list if there are no matching associations
     */
    public List<Project> getProjectsForContact(String contactName) {
        return projectContacts.stream().filter(link -> link.getContactName().equals(contactName))
                .map(ProjectContact::getProjectId).distinct()
                .flatMap(id -> projects.stream().filter(project -> project.getId() == id))
                .sorted(Comparator.comparingInt(Project::getId)).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        setProjects(newData.getProjectList());
        setProjectContacts(newData.getProjectContacts());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons) && projects.equals(otherAddressBook.projects)
                && projectContacts.equals(otherAddressBook.projectContacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, projects, projectContacts);
    }
}
