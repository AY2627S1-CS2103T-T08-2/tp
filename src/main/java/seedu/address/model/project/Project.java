package seedu.address.model.project;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a Project in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Project {

    // Identity fields
    private final ProjectId projectId;


    // Data fields
    private final ProjectName name;
    private final Deadline deadline;
    private final Description description;

    /**
     * Creates a new project with a generated identifier.
     */
    public Project(ProjectName name, Deadline deadline, Description description) {
        this(ProjectId.generate(), name, deadline, description);
    }

    /**
     * Constructs a project with an existing identifier.
     * Used when restoring or editing a project.
     */
    public Project(ProjectId projectId, ProjectName name,
                   Deadline deadline, Description description) {
        requireAllNonNull(projectId, name, deadline, description);
        this.projectId = projectId;
        this.name = name;
        this.deadline = deadline;
        this.description = description;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public ProjectName getName() {
        return name;
    }

    public Deadline getDeadline() {
        return deadline;
    }

    public Description getDescription() {
        return description;
    }

    /**
     * Returns true if both projects have the same identifier.
     * This defines a weaker notion of equality between two projects.
     */
    public boolean isSameProject(Project otherProject) {
        if (otherProject == this) {
            return true;
        }

        return otherProject != null
                && projectId.equals(otherProject.projectId);
    }

    /**
     * Returns true if both projects have the same identity and data fields.
     * This defines a stronger notion of equality between two projects.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Project otherProject)) {
            return false;
        }

        return projectId.equals(otherProject.projectId)
                && name.equals(otherProject.name)
                && deadline.equals(otherProject.deadline)
                && description.equals(otherProject.description);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(projectId, name, deadline, description);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("projectId", projectId)
                .add("name", name)
                .add("deadline", deadline)
                .add("description", description)
                .toString();
    }
}
