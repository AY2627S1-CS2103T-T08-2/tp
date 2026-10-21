package seedu.address.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Returns true if a project with the same identity as {@code project} exists in the address book.
     */
    boolean hasProject(Project project);

    /**
     * Deletes the given project.
     * The project must exist in the address book.
     */
    void deleteProject(Project target);

    /**
     * Adds the given project.
     * {@code project} must not already exist in the address book.
     */
    void addProject(Project project);

    /**
     * Replaces the given project {@code target} with {@code editedProject}.
     * {@code target} must exist in the address book.
     * The project identity of {@code editedProject} must not be the same as another existing project in the address
     * book.
     */
    void setProject(Project target, Project editedProject);

    /**
     * Returns an unmodifiable view of the project list.
     */
    ObservableList<Project> getProjectList();

    /**
     * Returns the project with the given name, or an empty Optional if none exists.
     */
    Optional<Project> findProject(ProjectName name);

    /**
     * Returns contacts associated with {@code project}, preserving address-book order.
     * The project must exist in the address book.
     */
    List<Person> getProjectMembers(Project project);

    /**
     * Returns true if {@code membership} exists in the address book.
     */
    boolean hasProjectMembership(ProjectMembership membership);

    /**
     * Adds a project membership to the address book.
     * The referenced person and project must exist, and the membership must not already exist.
     */
    void addProjectMembership(ProjectMembership membership);

    /**
     * Removes the given project membership from the address book.
     * The membership must exist in the address book.
     */
    void removeProjectMembership(ProjectMembership membership);

    /**
     * Returns the current contact filter, or null if no filter is set.
     */
    Predicate<? super Person> getFilteredPersonListPredicate();
}
