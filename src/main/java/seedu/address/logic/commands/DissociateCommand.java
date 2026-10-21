package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/**
 * Dissociates a contact identified by its displayed index from a project identified by its list index.
 */
public class DissociateCommand extends Command {

    public static final String COMMAND_WORD = "dissociate";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Dissociates a contact from a project using their list indices.\n"
            + "Parameters: " + PREFIX_CONTACT + "CONTACT_INDEX " + PREFIX_PROJECT
            + "PROJECT_INDEX (both must be positive integers)\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_CONTACT + "2 " + PREFIX_PROJECT + "1";

    public static final String MESSAGE_SUCCESS = "Dissociated contact \"%1$s\" from project \"%2$s\".";
    public static final String MESSAGE_NOT_ASSOCIATED =
            "Contact \"%1$s\" is not associated with project \"%2$s\".";

    private final Index contactIndex;
    private final Index projectIndex;

    /**
     * Creates a command to dissociate the contact and project at the given indices.
     */
    public DissociateCommand(Index contactIndex, Index projectIndex) {
        requireAllNonNull(contactIndex, projectIndex);
        this.contactIndex = contactIndex;
        this.projectIndex = projectIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> displayedContacts = model.getFilteredPersonList();
        List<Project> projects = model.getProjectList();

        if (contactIndex.getZeroBased() >= displayedContacts.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        if (projectIndex.getZeroBased() >= projects.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
        }

        Person contact = displayedContacts.get(contactIndex.getZeroBased());
        Project project = projects.get(projectIndex.getZeroBased());
        ProjectMembership membership = new ProjectMembership(contact.getPersonId(), project.getProjectId());

        if (!model.hasProjectMembership(membership)) {
            throw new CommandException(String.format(
                    MESSAGE_NOT_ASSOCIATED, contact.getName(), project.getName()));
        }

        model.removeProjectMembership(membership);

        return new CommandResult(String.format(
                MESSAGE_SUCCESS, contact.getName(), project.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DissociateCommand otherDissociateCommand)) {
            return false;
        }

        return contactIndex.equals(otherDissociateCommand.contactIndex)
                && projectIndex.equals(otherDissociateCommand.projectIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("contactIndex", contactIndex)
                .add("projectIndex", projectIndex)
                .toString();
    }
}
