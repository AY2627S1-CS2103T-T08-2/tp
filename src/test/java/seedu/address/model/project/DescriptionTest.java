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
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidDescription = "\n";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidDescription));
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
        assertFalse(Description.isValidDescription(
                        """
                        tttttttttttttttttttttttttttttttttttttttttttttttttt
                        tttttttttttttttttttttttttttttttttttttttttttttttttt
                        tttttttttttttttttttttttttttttttttttttttttttttttttt
                        tttttttttttttttttttttttttttttttttttttttttttttttttt
                        t"""
        )); // too long at 201 characters

        // valid description
        assertTrue(Description.isValidDescription("peter jack")); // alphabets only
        assertTrue(Description.isValidDescription("12345")); // numbers only
        assertTrue(Description.isValidDescription("peter the 2nd")); // alphanumeric characters
        assertTrue(Description.isValidDescription("Capital Tan")); // with capital letters
        assertTrue(Description.isValidDescription("David Roger Jackson Ray Jr 2nd")); // long names
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
