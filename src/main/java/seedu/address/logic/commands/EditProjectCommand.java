package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PROJECTS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;

/**
 * Edits the details of an existing project in UniTeam.
 */
public class EditProjectCommand extends Command {

    public static final String COMMAND_WORD = "editproject";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the project identified "
        + "by the index number used in the displayed project list. "
        + "Existing values will be overwritten by the input values.\n"
        + "Parameters: INDEX (must be a positive integer) "
        + "[" + PREFIX_NAME + "PROJECT_NAME] "
        + "[" + PREFIX_DEADLINE + "DEADLINE] "
        + "[" + PREFIX_DESCRIPTION + "DESCRIPTION] "
        + "Example: " + COMMAND_WORD + " 1 "
        + PREFIX_NAME + "New Project Name "
        + PREFIX_DEADLINE + "2027-01-01";

    public static final String MESSAGE_EDIT_PROJECT_SUCCESS = "Edited project: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PROJECT = "This project already exists in UniTeam.";

    private final Index index;
    private final EditProjectDescriptor editProjectDescriptor;

    /**
     * @param index                 of the project in the filtered project list to edit
     * @param editProjectDescriptor details to edit the project with
     */
    public EditProjectCommand(Index index, EditProjectDescriptor editProjectDescriptor) {
        requireNonNull(index);
        requireNonNull(editProjectDescriptor);

        this.index = index;
        this.editProjectDescriptor = new EditProjectDescriptor(editProjectDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Project> lastShownList = model.getFilteredProjectList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
        }

        Project projectToEdit = lastShownList.get(index.getZeroBased());
        Project editedProject = createEditedProject(projectToEdit, editProjectDescriptor);

        boolean hasDuplicateName = model
            .findProject(editedProject.getName())
            .filter(project -> !project.isSameProject(projectToEdit))
            .isPresent();

        if (hasDuplicateName) {
            throw new CommandException(MESSAGE_DUPLICATE_PROJECT);
        }

        model.setProject(projectToEdit, editedProject);
        model.updateFilteredProjectList(PREDICATE_SHOW_ALL_PROJECTS);
        return new CommandResult(String.format(MESSAGE_EDIT_PROJECT_SUCCESS, Messages.format(editedProject)));
    }

    /**
     * Creates and returns a {@code Project} with the details of {@code projectToEdit}
     * edited with {@code editProjectDescriptor}.
     */
    private static Project createEditedProject(Project projectToEdit, EditProjectDescriptor editProjectDescriptor) {
        assert projectToEdit != null;

        ProjectName updatedName = editProjectDescriptor
            .getName()
            .orElse(projectToEdit.getName());
        Deadline updatedDeadline = editProjectDescriptor
            .getDeadline()
            .orElse(projectToEdit.getDeadline());
        Description updatedDescription = editProjectDescriptor
            .getDescription()
            .orElse(projectToEdit.getDescription());

        return new Project(projectToEdit.getProjectId(), updatedName,
            updatedDeadline, updatedDescription);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditProjectCommand otherEditProjectCommand)) {
            return false;
        }

        return index.equals(otherEditProjectCommand.index)
            && editProjectDescriptor.equals(otherEditProjectCommand.editProjectDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .add("index", index)
            .add("editProjectDescriptor", editProjectDescriptor)
            .toString();
    }

    /**
     * Stores the details to edit the project with. Each non-empty field value will replace the
     * corresponding field value of the project. Omitted fields remain unchanged and an
     * empty description clears it.
     */
    public static class EditProjectDescriptor {
        private ProjectName name;
        private Deadline deadline;
        private Description description;

        public EditProjectDescriptor() {
        }

        /**
         * Copy constructor.
         */
        public EditProjectDescriptor(EditProjectDescriptor toCopy) {
            setName(toCopy.name);
            setDeadline(toCopy.deadline);
            setDescription(toCopy.description);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, deadline, description);
        }

        public void setName(ProjectName name) {
            this.name = name;
        }

        public Optional<ProjectName> getName() {
            return Optional.ofNullable(name);
        }

        public void setDeadline(Deadline deadline) {
            this.deadline = deadline;
        }

        public Optional<Deadline> getDeadline() {
            return Optional.ofNullable(deadline);
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public Optional<Description> getDescription() {
            return Optional.ofNullable(description);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditProjectDescriptor otherEditProjectDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditProjectDescriptor.name)
                && Objects.equals(deadline, otherEditProjectDescriptor.deadline)
                && Objects.equals(description, otherEditProjectDescriptor.description);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .add("name", name)
                .add("deadline", deadline)
                .add("description", description)
                .toString();
        }
    }
}
