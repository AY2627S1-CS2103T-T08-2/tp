package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

/**
 * Represents an optional remark about a person in the address book.
 * Remarks may be empty because users can remove an existing remark.
 */
public class Remark {

    public final String value;

    /**
     * Creates a {@code Remark} containing {@code value}.
     */
    public Remark(String value) {
        requireNonNull(value);
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Remark otherRemark
                && value.equals(otherRemark.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
