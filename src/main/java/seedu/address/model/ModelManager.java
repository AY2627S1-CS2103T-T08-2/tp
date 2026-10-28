package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final FilteredList<Project> filteredProjects;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        filteredProjects = new FilteredList<>(this.addressBook.getProjectList());
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);
        addressBook.setPerson(target, editedPerson);
    }

    @Override
    public boolean hasProject(Project project) {
        requireNonNull(project);
        return addressBook.hasProject(project);
    }

    @Override
    public void deleteProject(Project target) {
        requireNonNull(target);
        addressBook.removeProject(target);
        refreshPersonFilter();
    }

    @Override
    public void addProject(Project project) {
        requireNonNull(project);
        addressBook.addProject(project);
        updateFilteredProjectList(PREDICATE_SHOW_ALL_PROJECTS);
    }

    @Override
    public void setProject(Project target, Project editedProject) {
        requireAllNonNull(target, editedProject);
        addressBook.setProject(target, editedProject);
    }

    @Override
    public Optional<Project> findProject(ProjectName name) {
        requireNonNull(name);
        return addressBook.getProjectList().stream()
                .filter(project -> project.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Person> getProjectMembers(Project project) {
        requireNonNull(project);
        return addressBook.getProjectMembers(project);
    }

    @Override
    public List<Project> getPersonProjects(Person person) {
        requireNonNull(person);
        return addressBook.getPersonProjects(person);
    }

    @Override
    public boolean hasProjectMembership(ProjectMembership membership) {
        requireNonNull(membership);
        return addressBook.hasProjectMembership(membership);
    }

    @Override
    public void addProjectMembership(ProjectMembership membership) {
        requireNonNull(membership);
        addressBook.addProjectMembership(membership);
        refreshPersonFilter();
    }

    @Override
    public void setProjectMembership(ProjectMembership membership) {
        requireNonNull(membership);
        addressBook.setProjectMembership(membership);
    }

    @Override
    public void removeProjectMembership(ProjectMembership membership) {
        requireNonNull(membership);
        addressBook.removeProjectMembership(membership);
        refreshPersonFilter();
    }

    /** Re-evaluates the current contact filter when membership changes without a person being edited. */
    private void refreshPersonFilter() {
        Predicate<? super Person> predicate = filteredPersons.getPredicate();
        if (predicate != null) {
            filteredPersons.setPredicate(null);
            filteredPersons.setPredicate(predicate);
        }
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public Predicate<? super Person> getFilteredPersonListPredicate() {
        return filteredPersons.getPredicate();
    }

    //=========== Filtered Project List Accessors =============================================================

    @Override
    public ObservableList<Project> getFilteredProjectList() {
        return filteredProjects;
    }

    @Override
    public void updateFilteredProjectList(Predicate<Project> predicate) {
        requireNonNull(predicate);
        filteredProjects.setPredicate(predicate);
    }

    @Override
    public Predicate<? super Project> getFilteredProjectListPredicate() {
        return filteredProjects.getPredicate();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
            && userPrefs.equals(otherModelManager.userPrefs)
            && filteredPersons.equals(otherModelManager.filteredPersons)
            && filteredProjects.equals(otherModelManager.filteredProjects);
    }
}
