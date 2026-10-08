package seedu.address.model.project;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

/**
 * Represents a project stored in the address book.
 */
public final class Project {
    private final int id;
    private final String name;

    /**
     * Creates a project with the given identifier and name.
     *
     * @param id unique, non-negative project identifier
     * @param name non-blank project name
     */
    public Project(int id, String name) {
        requireNonNull(name);
        if (id < 0 || name.isBlank()) {
            throw new IllegalArgumentException("Project id must be non-negative and name must not be blank.");
        }
        this.id = id;
        this.name = name;
    }

    /** Returns this project's identifier. */
    public int getId() {
        return id;
    }

    /** Returns this project's name. */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Project project && id == project.id && name.equals(project.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Project{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
