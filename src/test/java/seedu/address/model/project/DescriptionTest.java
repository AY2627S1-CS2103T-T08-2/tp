package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Name;

public class DescriptionTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Description(null));
    }

    @Test
    public void constructor_invalidDescription_throwsIllegalArgumentException() {
        String invalidDescription = "\n";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidDescription));
        assertThrows(IllegalArgumentException.class,
                () -> new Description("a".repeat(201)));
    }

    @Test
    public void isValidDescription() {
        // null description
        assertThrows(NullPointerException.class, () -> Description.isValidDescription(null));

        // invalid description
        assertFalse(Description.isValidDescription("\n"));
        assertFalse(Description.isValidDescription("\r"));
        assertFalse(Description.isValidDescription("\u0085"));
        assertFalse(Description.isValidDescription("\u2028"));
        assertFalse(Description.isValidDescription("\u2029"));
        assertFalse(Description.isValidDescription("a".repeat(201))); // too long

        // valid description
        assertTrue(Description.isValidDescription("peter jack")); // alphabets only
        assertTrue(Description.isValidDescription("12345")); // numbers only
        assertTrue(Description.isValidDescription("peter the 2nd")); // alphanumeric characters
        assertTrue(Description.isValidDescription("Capital Tan")); // with capital letters
        assertTrue(Description.isValidDescription("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Description.isValidDescription(""));
        assertTrue(Description.isValidDescription("Build UniTeam! (Version 1.0)"));
        assertTrue(Description.isValidDescription("😀".repeat(200)));
    }

    @Test
    public void equals() {
        Description description = new Description("This is a valid description");

        // same values -> returns true
        assertTrue(description.equals(new Description("This is a valid description")));

        // same object -> returns true
        assertTrue(description.equals(description));

        // null -> returns false
        assertFalse(description.equals(null));

        // different types -> returns false
        assertFalse(description.equals(5.0f));

        // different values -> returns false
        assertFalse(description.equals(new Description("This is another valid description")));
    }
}
