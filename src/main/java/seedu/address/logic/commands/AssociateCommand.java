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
 * Associates a contact identified by its displayed index with a project identified by its list index.
 */
public class AssociateCommand extends Command {

    public static final String COMMAND_WORD = "associate";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Associates a contact with a project using their list indices.\n"
            + "Parameters: " + PREFIX_CONTACT + "CONTACT_INDEX " + PREFIX_PROJECT
            + "PROJECT_INDEX (both must be positive integers)\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_CONTACT + "2 " + PREFIX_PROJECT + "1";

    public static final String MESSAGE_SUCCESS = "Associated contact \"%1$s\" with project \"%2$s\".";
    public static final String MESSAGE_DUPLICATE_ASSOCIATION =
            "Contact \"%1$s\" is already associated with project \"%2$s\".";

    private final Index contactIndex;
    private final Index projectIndex;

    /**
     * Creates a command to associate the contact and project at the given indices.
     */
    public AssociateCommand(Index contactIndex, Index projectIndex) {
        requireAllNonNull(contactIndex, projectIndex);
        this.contactIndex = contactIndex;
        this.projectIndex = projectIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> displayedContacts = model.getFilteredPersonList();
        List<Project> projects = model.getFilteredProjectList();

        if (contactIndex.getZeroBased() >= displayedContacts.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        if (projectIndex.getZeroBased() >= projects.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
        }

        Person contact = displayedContacts.get(contactIndex.getZeroBased());
        Project project = projects.get(projectIndex.getZeroBased());
        ProjectMembership membership = new ProjectMembership(contact.getPersonId(), project.getProjectId());

        if (model.hasProjectMembership(membership)) {
            throw new CommandException(String.format(
                    MESSAGE_DUPLICATE_ASSOCIATION, contact.getName(), project.getName()));
        }

        model.addProjectMembership(membership);

        return new CommandResult(String.format(
                MESSAGE_SUCCESS, contact.getName(), project.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AssociateCommand otherAssociateCommand)) {
            return false;
        }

        return contactIndex.equals(otherAssociateCommand.contactIndex)
                && projectIndex.equals(otherAssociateCommand.projectIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("contactIndex", contactIndex)
                .add("projectIndex", projectIndex)
                .toString();
    }
}
