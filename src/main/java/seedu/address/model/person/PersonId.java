package seedu.address.model.person;

import java.util.UUID;

/**
 * Represents a PersonId for a Person.
 * Guarantees: Immutable ID.
 */
public class PersonId {
    
    private final UUID id;

    public PersonId(UUID id) {
        this.id = id;
    }

    public UUID generateId() {
        return id;
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
