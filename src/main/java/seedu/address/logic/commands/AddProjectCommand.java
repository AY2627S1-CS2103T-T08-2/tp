package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;

/**
 * Adds a project to the address book.
 */
public class AddProjectCommand extends Command {

    public static final String COMMAND_WORD = "addproject";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a project to the address book. "
            + "Parameters: n/PROJECT_NAME d/DEADLINE [desc/DESCRIPTION]\n"
            + "Example: " + COMMAND_WORD
            + " n/CS2103T Team Project d/2026-11-13"
            + " desc/Build a contact management application";

    public static final String MESSAGE_SUCCESS = "New project added:\n%1$s\nDeadline: %2$s\nDescription: %3$s";
    public static final String MESSAGE_DUPLICATE_PROJECT = "This project already exists in the address book.";

    private final Project toAdd;

    /**
     * Creates an AddProjectCommand to add the specified {@code Project}
     */
    public AddProjectCommand(Project project) {
        requireNonNull(project);
        toAdd = project;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasProject(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PROJECT);
        }

        model.addProject(toAdd);

        String description = toAdd.getDescription().equals(Description.EMPTY)
                ? "No description provided"
                : toAdd.getDescription().toString();

        return new CommandResult(String.format(
                MESSAGE_SUCCESS,
                toAdd.getName(),
                toAdd.getDeadline(),
                description
        ));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddProjectCommand otherAddProjectCommand)) {
            return false;
        }

        return toAdd.equals(otherAddProjectCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
