package seedu.address.model.project;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

/**
 * Represents a Project's deadline in the address book.
 * Guarantees: immutable
 */
public class Deadline {

    public final LocalDate value;

    /**
     * Constructs a {@code Deadline}.
     *
     * @param deadline A valid date.
     */
    public Deadline(LocalDate deadline) {
        requireNonNull(deadline);
        value = deadline;
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

        // instanceof handles nulls
        if (!(other instanceof Deadline otherDeadline)) {
            return false;
        }

        return value.equals(otherDeadline.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
