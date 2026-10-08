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
    private final ProjectName name;

    // Data fields
    private final Deadline deadline;
    private final Description description;

    /**
     * Every field must be present and not null.
     */
    public Project(ProjectName name, Deadline deadline, Description description) {
        requireAllNonNull(name, deadline, description);
        this.name = name;
        this.deadline = deadline;
        this.description = description;
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
     * Returns true if both projects have the same name.
     * This defines a weaker notion of equality between two projects.
     */
    public boolean isSameProject(Project otherProject) {
        if (otherProject == this) {
            return true;
        }

        return otherProject != null
                && otherProject.getName().equals(getName());
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

        return name.equals(otherProject.name)
                && deadline.equals(otherProject.deadline)
                && description.equals(otherProject.description);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, deadline, description);
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
