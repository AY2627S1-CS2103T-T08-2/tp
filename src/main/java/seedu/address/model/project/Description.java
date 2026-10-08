package seedu.address.model.project;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Project's description in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidDescription(String)}
 */
public class Description {

    public static final Description EMPTY = new Description("");

    public static final String MESSAGE_CONSTRAINTS =
            "Descriptions must be at most 200 characters long and must not contain line breaks";

    public static final String VALIDATION_REGEX = "[^\\r\\n\\u0085\\u2028\\u2029]*";

    public final String description;

    /**
     * Constructs a {@code Description}.
     *
     * @param description A valid description.
     */
    public Description(String description) {
        requireNonNull(description);
        checkArgument(isValidDescription(description), MESSAGE_CONSTRAINTS);
        this.description = description;
    }

    /**
     * Returns true if a given string is a valid description.
     */
    public static boolean isValidDescription(String test) {
        return test.codePointCount(0, test.length()) <= 200
                && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Description otherDescription)) {
            return false;
        }

        return description.equals(otherDescription.description);
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }
}
