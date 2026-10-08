package seedu.address.model.project;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Project's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class ProjectName {

    public static final String MESSAGE_CONSTRAINTS =
            "Project names must be 1–50 characters long, contain at least one letter or digit, "
                    + "and contain only letters, digits, spaces, hyphens, apostrophes, "
                    + "ampersands and parentheses";

    public static final String VALIDATION_REGEX = "[\\\\p{L}\\\\p{N} '&()\\\\-]+";

    public final String projectName;

    /**
     * Constructs a {@code ProjectName}.
     *
     * @param name A valid name.
     */
    public ProjectName(String name) {
        requireNonNull(name);
        String normalised = normalise(name);
        checkArgument(isValidName(normalised), MESSAGE_CONSTRAINTS);
        projectName = normalised;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        String normalised = normalise(test);
        int length = normalised.codePointCount(0, normalised.length());
        return length >= 1 && length <= 50
                && normalised.matches(VALIDATION_REGEX)
                && normalised.codePoints().anyMatch(Character::isLetterOrDigit);
    }

    @Override
    public String toString() {
        return projectName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ProjectName otherName)) {
            return false;
        }

        return projectName.equals(otherName.projectName);
    }

    @Override
    public int hashCode() {
        return projectName.hashCode();
    }

    private static String normalise(String name) {
        return name.strip().replaceAll("\\s+", " ");
    }
}
