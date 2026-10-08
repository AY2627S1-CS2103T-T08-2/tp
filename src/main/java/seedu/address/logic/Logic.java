package seedu.address.logic;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/**
 * API of the Logic component
 */
public interface Logic {
    /**
     * Executes the command and returns the result.
     * @param commandText The command as entered by the user.
     * @return the result of the command execution.
     * @throws CommandException If an error occurs during command execution.
     * @throws ParseException If an error occurs during parsing.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /** Returns an unmodifiable view of the filtered list of persons */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Set the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Returns all projects in ascending ID order.
     *
     * @return projects sorted by ID
     */
    default List<Project> getProjects() {
        return List.of();
    }

    /**
     * Returns the contacts assigned to a project.
     *
     * @param projectId identifier of the project
     * @return associated contacts, or an empty list when there are no matches
     */
    default List<Person> getContactsForProject(int projectId) {
        return List.of();
    }

    /**
     * Returns the projects assigned to a contact.
     *
     * @param contactName unique name of the contact
     * @return associated projects, or an empty list when there are no matches
     */
    default List<Project> getProjectsForContact(String contactName) {
        return List.of();
    }
}
