package seedu.address.model.membership;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.AppUtil;

/**
 * Describes the task allotment for a person's membership in one project.
 * Guarantees: immutable; contains no line breaks and is at most 200 characters.
 */
public final class Role {

    public static final Role EMPTY = new Role("");
    public static final String MESSAGE_CONSTRAINTS =
            "Roles must be at most 200 characters long and must not contain line breaks";
    public static final String VALIDATION_REGEX = "[^\\r\\n\\u0085\\u2028\\u2029]*";

    private final String description;

    /** Creates a role from a validated description. */
    public Role(String description) {
        requireNonNull(description);
        AppUtil.checkArgument(isValidRole(description), MESSAGE_CONSTRAINTS);
        this.description = description;
    }

    /** Returns whether the description is a valid role. */
    public static boolean isValidRole(String description) {
        return description.codePointCount(0, description.length()) <= 200
                && description.matches(VALIDATION_REGEX);
    }

    /** Returns the role description. */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Role otherRole && description.equals(otherRole.description));
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }
}
