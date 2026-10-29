package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;

/**
 * Lists the contacts associated with a named project.
 */
public class ListMembersCommand extends Command {

    public static final String COMMAND_WORD = "listmembers";
    public static final String MESSAGE_USAGE = COMMAND_WORD
        + ": Lists contacts associated with a project.\n"
        + "Parameters: pr/PROJECT_NAME\n"
        + "Example: " + COMMAND_WORD + " pr/CS2103T Team Project";
    public static final String MESSAGE_PROJECT_NOT_FOUND = "Project \"%1$s\" does not exist.";
    public static final String MESSAGE_NO_MEMBERS = "No contacts are associated with project \"%1$s\".";
    public static final String MESSAGE_SUCCESS = "Contacts in project \"%1$s\" (%2$d):\n%3$s";

    private static final Logger logger = LogsCenter.getLogger(ListMembersCommand.class);

    private final ProjectName projectName;

    /**
     * Creates a command to list the contacts associated with {@code projectName}.
     */
    public ListMembersCommand(ProjectName projectName) {
        requireNonNull(projectName);
        this.projectName = projectName;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Project project = model
            .findProject(projectName)
            .orElseThrow(() -> new CommandException(
                String.format(MESSAGE_PROJECT_NOT_FOUND, projectName)));
        List<Person> members = model.getProjectMembers(project);
        model.updateFilteredPersonList(person -> model.hasProjectMembership(
            new ProjectMembership(person.getPersonId(), project.getProjectId())));
        assert model
            .getFilteredPersonList()
            .size() == members.size()
            : "Displayed members must match project membership";
        logger.fine("Showing " + members.size() + " project member cards");
        if (members.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_NO_MEMBERS, projectName),
                CommandResult.ListViewTarget.CONTACTS);
        }

        Map<PersonId, ProjectMembership> membershipsByPersonId = model
            .getAddressBook()
            .getProjectMembershipList()
            .stream()
            .filter(membership -> membership
                .getProjectId()
                .equals(project.getProjectId()))
            .collect(Collectors.toMap(ProjectMembership::getPersonId, Function.identity()));
        String memberNames = IntStream
            .range(0, members.size())
            .mapToObj(index -> formatMember(index + 1, members.get(index),
                membershipsByPersonId.get(members
                    .get(index)
                    .getPersonId())))
            .collect(Collectors.joining("\n"));
        return new CommandResult(String.format(MESSAGE_SUCCESS, projectName, members.size(), memberNames),
            CommandResult.ListViewTarget.CONTACTS);
    }

    private String formatMember(int index, Person person, ProjectMembership membership) {
        String role = membership
            .getRole()
            .toString();
        return role.isEmpty() ? String.format("%1$d. %2$s", index, person.getName())
            : String.format("%1$d. %2$s — %3$s", index, person.getName(), role);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ListMembersCommand otherCommand)) {
            return false;
        }
        return projectName.equals(otherCommand.projectName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .add("projectName", projectName)
            .toString();
    }
}
