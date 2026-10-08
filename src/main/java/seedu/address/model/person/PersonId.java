package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

/**
 * Represents a PersonId for a Person.
 * Guarantees: ID is present, not null, and immutable.
 */
public class PersonId {

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
