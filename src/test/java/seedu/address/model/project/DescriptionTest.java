package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class DescriptionTest {

    @Test
    public void constructor_lengthBoundaries_countsCodePoints() {
        String supplementaryCharacter = new String(Character.toChars(0x1f600));
        for (String character : new String[] {"a", supplementaryCharacter}) {
            for (int length : new int[] {0, 1, 199, 200}) {
                String value = character.repeat(length);
                assertTrue(Description.isValidDescription(value));
                assertEquals(value, new Description(value).toString());
            }
            String tooLong = character.repeat(201);
            assertFalse(Description.isValidDescription(tooLong));
            assertThrows(IllegalArgumentException.class, () -> new Description(tooLong));
        }
        assertEquals(Description.EMPTY, new Description(""));
    }

    @Test
    public void constructor_lineBreakAtAnyPosition_throwsIllegalArgumentException() {
        for (String lineBreak : new String[] {"\n", "\r", "\r\n", "\u0085", "\u2028", "\u2029"}) {
            for (String value : new String[] {lineBreak + "a", "a" + lineBreak + "b", "a" + lineBreak}) {
                assertFalse(Description.isValidDescription(value));
                assertThrows(IllegalArgumentException.class, () -> new Description(value));
            }
        }
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Description(null));
    }

    @Test
    public void constructor_invalidDescription_throwsIllegalArgumentException() {
        String invalidDescription = "\n";
        assertThrows(IllegalArgumentException.class, () -> new Description(invalidDescription));
        assertThrows(IllegalArgumentException.class, () -> new Description("a".repeat(201)));
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
