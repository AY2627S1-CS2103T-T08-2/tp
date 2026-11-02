package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone display text in the address book.
 * Guarantees: immutable; contains non-blank text without ISO control characters
 * as declared in {@link #isValidPhone(String)}.
 */
public class Phone {


    public static final String MESSAGE_CONSTRAINTS =
            "Phone display text must not be blank or contain control characters";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone Valid phone display text.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    /**
     * Returns true if the given string is non-blank and contains no ISO control characters.
     */
    public static boolean isValidPhone(String test) {
        boolean hasContent = test.codePoints().anyMatch(codePoint ->
                !Character.isWhitespace(codePoint) && !Character.isSpaceChar(codePoint));
        return hasContent && test.codePoints().noneMatch(Character::isISOControl);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
