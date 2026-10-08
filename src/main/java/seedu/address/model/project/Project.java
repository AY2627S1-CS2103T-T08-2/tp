package seedu.address.model.project;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

/**
 * Represents a Project in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Project {
    public static final Description EMPTY = new Description("");

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
}
