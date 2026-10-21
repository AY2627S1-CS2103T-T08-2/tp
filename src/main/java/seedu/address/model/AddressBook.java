package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.UniqueProjectMembershipList;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectId;
import seedu.address.model.project.UniqueProjectList;
import seedu.address.model.project.exceptions.ProjectNotFoundException;

/**
 * Stores UniTeam's contacts, projects and project memberships.
 * Contacts have unique identifiers and names.
 * Projects have unique identifiers and normalised names.
 * Memberships are unique contact-project pairs that reference existing entities.
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final UniqueProjectList projects = new UniqueProjectList();
    private final UniqueProjectMembershipList memberships = new UniqueProjectMembershipList();

    public AddressBook() {
    }

    /**
     * Creates an AddressBook containing the contacts, projects and memberships from {@code toBeCopied}.
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
     * Replaces the contents of the project list with {@code projects}.
     * {@code projects} must not contain duplicate projects.
     */
    public void setProjects(List<Project> projects) {
        this.projects.setProjects(projects);
    }

    /**
     * Replaces the contents of the project membership list with {@code memberships}.
     * Every membership must reference a person and project in this address book, and no duplicates are allowed.
     */
    public void setProjectMemberships(List<ProjectMembership> memberships) {
        UniqueProjectMembershipList replacement = new UniqueProjectMembershipList();
        replacement.setMemberships(memberships);
        for (ProjectMembership membership : replacement) {
            ensureMembershipReferencesExist(membership);
        }
        this.memberships.setMemberships(replacement);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        setProjects(newData.getProjectList());
        setProjectMemberships(newData.getProjectMembershipList());
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
        memberships.removeAllForPerson(key.getPersonId());
    }

    //// project-level operations

    /**
     * Returns true if a project with the same identity as {@code project} exists in the address book.
     */
    public boolean hasProject(Project project) {
        requireNonNull(project);
        return projects.contains(project);
    }

    /**
     * Adds a project to the address book.
     * The project must not already exist in the address book.
     */
    public void addProject(Project p) {
        projects.add(p);
    }

    /**
     * Replaces the given project {@code target} in the list with {@code editedProject}.
     * {@code target} must exist in the address book.
     * The project identity of {@code editedProject} must not be the same as another existing project in the address
     * book.
     */
    public void setProject(Project target, Project editedProject) {
        requireNonNull(editedProject);
        projects.setProject(target, editedProject);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removeProject(Project key) {
        projects.remove(key);
        memberships.removeAllForProject(key.getProjectId());
    }

    //// project-membership-level operations

    /**
     * Returns true if {@code membership} exists in the address book.
     */
    public boolean hasProjectMembership(ProjectMembership membership) {
        requireNonNull(membership);
        return memberships.contains(membership);
    }

    /**
     * Adds a project membership to the address book.
     * The referenced person and project must exist, and the membership must not already exist.
     */
    public void addProjectMembership(ProjectMembership membership) {
        requireNonNull(membership);
        ensureMembershipReferencesExist(membership);
        memberships.add(membership);
    }

    /** Replaces an existing membership while preserving its person and project identifiers. */
    public void setProjectMembership(ProjectMembership membership) {
        requireNonNull(membership);
        ensureMembershipReferencesExist(membership);
        memberships.set(membership);
    }

    /**
     * Returns the contacts associated with {@code project}, preserving address-book order.
     *
     * @throws ProjectNotFoundException if the project is not in this address book.
     */
    public List<Person> getProjectMembers(Project project) {
        requireNonNull(project);
        if (!projects.contains(project)) {
            throw new ProjectNotFoundException();
        }

        Set<PersonId> memberIds = memberships.asUnmodifiableObservableList().stream()
                .filter(membership -> membership.getProjectId().equals(project.getProjectId()))
                .map(ProjectMembership::getPersonId)
                .collect(Collectors.toSet());

        return persons.asUnmodifiableObservableList().stream()
                .filter(person -> memberIds.contains(person.getPersonId()))
                .toList();
    }

    /**
     * Removes {@code membership} from the address book.
     * The membership must exist in the address book.
     */
    public void removeProjectMembership(ProjectMembership membership) {
        requireNonNull(membership);
        memberships.remove(membership);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("projects", projects)
                .add("memberships", memberships)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Project> getProjectList() {
        return projects.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<ProjectMembership> getProjectMembershipList() {
        return memberships.asUnmodifiableObservableList();
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

        return persons.equals(otherAddressBook.persons)
                && projects.equals(otherAddressBook.projects)
                && memberships.equals(otherAddressBook.memberships);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, projects, memberships);
    }

    /**
     * Throws an exception if the membership references a person or project that is not in this address book.
     */
    private void ensureMembershipReferencesExist(ProjectMembership membership) {
        if (!hasPersonId(membership.getPersonId())) {
            throw new PersonNotFoundException();
        }
        if (!hasProjectId(membership.getProjectId())) {
            throw new ProjectNotFoundException();
        }
    }

    /**
     * Returns true if a person with {@code personId} exists in the address book.
     */
    private boolean hasPersonId(PersonId personId) {
        return persons.containsId(personId);
    }

    /**
     * Returns true if a project with {@code projectId} exists in the address book.
     */
    private boolean hasProjectId(ProjectId projectId) {
        return projects.asUnmodifiableObservableList().stream()
                .anyMatch(project -> project.getProjectId().equals(projectId));
    }
}
