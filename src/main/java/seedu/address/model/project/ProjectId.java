package seedu.address.model.project;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

/**
 * Represents a Project's stable identifier.
 * Guarantees: immutable; value is not null.
 */
public class ProjectId {

    public final UUID value;

    /**
     * Constructs a {@code ProjectId}.
     *
     * @param value A valid UUID.
     */
    public ProjectId(UUID value) {
        requireNonNull(value);
        this.value = value;
    }

    public static ProjectId generate() {
        return new ProjectId(UUID.randomUUID());
    }

    /**
     * Constructs an identifier from its stored string representation.
     *
     * @throws NullPointerException if {@code value} is null.
     * @throws IllegalArgumentException if {@code value} cannot be parsed as a UUID.
     */
    public static ProjectId fromString(String value) {
        requireNonNull(value);
        return new ProjectId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }


    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ProjectId otherProjectId)) {
            return false;
        }

        return value.equals(otherProjectId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
