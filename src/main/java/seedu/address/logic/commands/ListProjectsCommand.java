package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.model.Model;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;

/** Lists all projects in UniTeam. */
public class ListProjectsCommand extends Command {

    public static final String COMMAND_WORD = "listprojects";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists all projects in UniTeam.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_EMPTY = "There are no projects to list.";
    public static final String MESSAGE_SUCCESS = "Listed %1$d project(s):\n%2$s";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<Project> projects = model.getProjectList();
        if (projects.isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }

        String projectDetails = IntStream.range(0, projects.size())
                .mapToObj(index -> formatProject(projects.get(index), index + 1))
                .collect(Collectors.joining("\n"));
        return new CommandResult(String.format(MESSAGE_SUCCESS, projects.size(), projectDetails));
    }

    private static String formatProject(Project project, int displayedIndex) {
        String description = project.getDescription().equals(Description.EMPTY)
                ? "No description provided"
                : project.getDescription().toString();
        return String.format("%1$d. %2$s | Deadline: %3$s | Description: %4$s",
                displayedIndex, project.getName(), project.getDeadline(), description);
    }
}
