package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PROJECTS;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;

/** Lists all projects or the projects associated with a displayed contact. */
public class ListProjectsCommand extends Command {

    public static final String COMMAND_WORD = "listprojects";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all projects or the projects associated with a displayed contact.\n"
            + "Parameters: [" + PREFIX_CONTACT + "CONTACT_INDEX] (must be a positive integer)\n"
            + "Examples: " + COMMAND_WORD + " or " + COMMAND_WORD + " " + PREFIX_CONTACT + "2";
    public static final String MESSAGE_EMPTY = "There are no projects to list.";
    public static final String MESSAGE_SUCCESS = "Listed %1$d project(s):\n%2$s";
    public static final String MESSAGE_NO_PROJECTS = "No projects are associated with contact \"%1$s\".";
    public static final String MESSAGE_CONTACT_PROJECTS = "Projects for contact \"%1$s\" (%2$d):\n%3$s";

    private final Optional<Index> contactIndex;

    /** Creates a command to list all projects. */
    public ListProjectsCommand() {
        contactIndex = Optional.empty();
    }

    /** Creates a command to list projects associated with the contact at {@code contactIndex}. */
    public ListProjectsCommand(Index contactIndex) {
        this.contactIndex = Optional.of(requireNonNull(contactIndex));
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (contactIndex.isPresent()) {
            return executeForContact(model, contactIndex.get());
        }

        model.updateFilteredProjectList(PREDICATE_SHOW_ALL_PROJECTS);
        List<Project> projects = model.getFilteredProjectList();
        if (projects.isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY, CommandResult.ListViewTarget.PROJECTS);
        }

        String projectDetails = IntStream.range(0, projects.size())
                .mapToObj(index -> formatProject(projects.get(index), index + 1))
                .collect(Collectors.joining("\n"));
        return new CommandResult(String.format(MESSAGE_SUCCESS, projects.size(), projectDetails),
                CommandResult.ListViewTarget.PROJECTS);
    }

    /** Reports all memberships without changing either filtered view. */
    private CommandResult executeForContact(Model model, Index index) throws CommandException {
        List<Person> displayedContacts = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedContacts.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person contact = displayedContacts.get(index.getZeroBased());

        List<Project> projects = model.getPersonProjects(contact);
        if (projects.isEmpty()) {
            return new CommandResult(
                String.format(MESSAGE_NO_PROJECTS, contact.getName())
            );
        }

        List<Project> displayedProjects = model.getFilteredProjectList();
        String projectDetails = projects.stream()
            .map(project -> formatProject(project, displayedProjects.indexOf(project) + 1))
            .collect(Collectors.joining("\n"));

        return new CommandResult(String.format(
            MESSAGE_CONTACT_PROJECTS,
            contact.getName(),
            projects.size(),
            projectDetails)
        );
    }

    /** Formats a visible index, or marks a hidden project when {@code displayedIndex} is zero. */
    private static String formatProject(Project project, int displayedIndex) {
        String description = project.getDescription().equals(Description.EMPTY)
                ? "No description provided"
                : project.getDescription().toString();
        // A contact can belong to a project hidden by the current project search.
        String prefix = displayedIndex == 0 ? "[not displayed] " : displayedIndex + ". ";
        return String.format("%1$s%2$s | Deadline: %3$s | Description: %4$s",
                prefix, project.getName(), project.getDeadline(), description);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ListProjectsCommand otherCommand)) {
            return false;
        }
        return contactIndex.equals(otherCommand.contactIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("contactIndex", contactIndex).toString();
    }
}
