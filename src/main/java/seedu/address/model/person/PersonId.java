package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

/**
 * Represents a PersonId for a Person.
 * Guarantees: ID is present, not null, and immutable.
 */
public class PersonId {

    public static final String MESSAGE_CONSTRAINTS =
            "Person IDs must use the standard UUID format.";

    private final UUID id;

    /**
     * Constructs a person identifier from an existing UUID.
     *
     * @param id A non-null UUID.
     * @throws NullPointerException if {@code id} is null.
     */
    public PersonId(UUID id) {
        this.id = requireNonNull(id);
    }

    /**
     * Returns a new randomly generated person identifier.
     */
    public static PersonId generate() {
        return new PersonId(UUID.randomUUID());
    }

    /**
     * Returns a person identifier parsed from the given UUID string.
     *
     * @throws NullPointerException if {@code text} is null.
     * @throws IllegalArgumentException if {@code text} is not a standard UUID.
     */
    public static PersonId fromString(String text) {
        requireNonNull(text);
        UUID parsedId = UUID.fromString(text);
        if (!parsedId.toString().equalsIgnoreCase(text)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        return new PersonId(parsedId);
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PersonId otherPersonId)) {
            return false;
        }

        return id.equals(otherPersonId.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
