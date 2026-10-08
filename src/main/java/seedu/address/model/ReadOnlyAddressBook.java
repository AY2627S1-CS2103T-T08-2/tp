package seedu.address.model;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectContact;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /** Returns the projects stored in the address book. */
    default List<Project> getProjectList() {
        return List.of();
    }

    /** Returns the project-contact associations stored in the address book. */
    default List<ProjectContact> getProjectContacts() {
        return List.of();
    }

}
