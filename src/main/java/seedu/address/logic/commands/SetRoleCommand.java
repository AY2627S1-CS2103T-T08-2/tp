package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CliSyntax;
import seedu.address.model.Model;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/** Sets the task allotment for an existing project member. */
public class SetRoleCommand extends Command {

    public static final String COMMAND_WORD = "setrole";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sets a member's role in a project.\nParameters: " + CliSyntax.PREFIX_CONTACT + "CONTACT_INDEX "
            + CliSyntax.PREFIX_PROJECT + "PROJECT_INDEX " + CliSyntax.PREFIX_ROLE + "ROLE\n"
            + "Example: " + COMMAND_WORD + " c/2 pr/1 r/Implement the parser";
    public static final String MESSAGE_NOT_ASSOCIATED = "Contact \"%1$s\" is not associated with project \"%2$s\".";
    public static final String MESSAGE_SUCCESS = "Set role for \"%1$s\" in project \"%2$s\": %3$s";

    private final Index contactIndex;
    private final Index projectIndex;
    private final Role role;

    /** Creates a command to update a membership role. */
    public SetRoleCommand(Index contactIndex, Index projectIndex, Role role) {
        requireNonNull(contactIndex);
        requireNonNull(projectIndex);
        requireNonNull(role);
        this.contactIndex = contactIndex;
        this.projectIndex = projectIndex;
        this.role = role;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> contacts = model.getFilteredPersonList();
        List<Project> projects = model.getProjectList();
        if (contactIndex.getZeroBased() >= contacts.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        if (projectIndex.getZeroBased() >= projects.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
        }
        Person contact = contacts.get(contactIndex.getZeroBased());
        Project project = projects.get(projectIndex.getZeroBased());
        ProjectMembership membership = new ProjectMembership(contact.getPersonId(), project.getProjectId());
        if (!model.hasProjectMembership(membership)) {
            throw new CommandException(String.format(MESSAGE_NOT_ASSOCIATED, contact.getName(), project.getName()));
        }
        model.setProjectMembership(membership.withRole(role));
        return new CommandResult(String.format(MESSAGE_SUCCESS, contact.getName(), project.getName(), role));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof SetRoleCommand otherCommand
                && contactIndex.equals(otherCommand.contactIndex)
                && projectIndex.equals(otherCommand.projectIndex)
                && role.equals(otherCommand.role));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("contactIndex", contactIndex)
                .add("projectIndex", projectIndex).add("role", role).toString();
    }
}
