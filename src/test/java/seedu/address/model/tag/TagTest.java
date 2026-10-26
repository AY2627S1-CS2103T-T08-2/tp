package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_singleCharacterAndAlphanumericNames_preservesValue() {
        String[] validNames = {"A", "0", "friends", "Team2026"};
        for (String name : validNames) {
            assertTrue(Tag.isValidTagName(name), name);
            assertEquals(name, new Tag(name).tagName);
            assertEquals("[" + name + "]", new Tag(name).toString());
        }
    }

    @Test
    public void constructor_invalidCharacters_throwsIllegalArgumentException() {
        String[] invalidNames = {"", " ", " friend", "friend ", "two words", "a-b", "a_b", "a\tb", "a\nb"};
        for (String name : invalidNames) {
            assertFalse(Tag.isValidTagName(name), name);
            assertThrows(IllegalArgumentException.class, () -> new Tag(name));
        }
    }

    @Test
    public void equals_sameValue_matchesHashCodeAndRemainsCaseSensitive() {
        Tag tag = new Tag("Team1");
        Tag copy = new Tag("Team1");
        assertEquals(tag, copy);
        assertEquals(copy, tag);
        assertEquals(tag.hashCode(), copy.hashCode());
        assertNotEquals(tag, new Tag("team1"));
        assertFalse(tag.equals(null));
        assertFalse(tag.equals("Team1"));
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

}
