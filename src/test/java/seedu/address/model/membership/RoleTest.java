package seedu.address.model.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests for {@link Role}. */
public class RoleTest {

    @Test
    public void constructor_lengthBoundaries_countsCodePoints() {
        String supplementaryCharacter = new String(Character.toChars(0x1f600));
        for (String character : new String[] {"a", supplementaryCharacter}) {
            for (int length : new int[] {0, 1, 199, 200}) {
                String value = character.repeat(length);
                assertTrue(Role.isValidRole(value));
                assertEquals(value, new Role(value).getDescription());
            }
            String tooLong = character.repeat(201);
            assertFalse(Role.isValidRole(tooLong));
            assertThrows(IllegalArgumentException.class, () -> new Role(tooLong));
        }
        assertEquals(Role.EMPTY, new Role(""));
    }

    @Test
    public void constructor_lineBreakAtAnyPosition_throwsIllegalArgumentException() {
        for (String lineBreak : new String[] {"\n", "\r", "\r\n", "\u0085", "\u2028", "\u2029"}) {
            for (String value : new String[] {lineBreak + "a", "a" + lineBreak + "b", "a" + lineBreak}) {
                assertFalse(Role.isValidRole(value));
                assertThrows(IllegalArgumentException.class, () -> new Role(value));
            }
        }
    }

    @Test
    public void isValidRole_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Role.isValidRole(null));
    }

    @Test
    public void isValidRole_validDescription_returnsTrue() {
        assertTrue(Role.isValidRole("Write documentation"));
        assertTrue(Role.isValidRole(""));
        assertTrue(Role.isValidRole("x".repeat(200)));
    }

    @Test
    public void isValidRole_invalidDescription_returnsFalse() {
        assertFalse(Role.isValidRole("line one\nline two"));
        assertFalse(Role.isValidRole("line one\rline two"));
        assertFalse(Role.isValidRole("line one" + Character.toString((char) 0x0085) + "line two"));
        assertFalse(Role.isValidRole("line one" + Character.toString((char) 0x2028) + "line two"));
        assertFalse(Role.isValidRole("line one" + Character.toString((char) 0x2029) + "line two"));
        assertFalse(Role.isValidRole("x".repeat(201)));
    }

    @Test
    public void constructor_nullDescription_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Role(null));
    }

    @Test
    public void constructor_invalidRole_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Role("x".repeat(201)));
    }

    @Test
    public void role_sameDescription_equals() {
        assertEquals(new Role("Review tests"), new Role("Review tests"));
    }

    @Test
    public void valueMethods_returnDescriptionAndCompareValues() {
        Role role = new Role("Review tests");

        assertEquals("Review tests", role.getDescription());
        assertEquals("Review tests", role.toString());
        assertEquals(role.hashCode(), new Role("Review tests").hashCode());
        assertTrue(role.equals(role));
        assertFalse(role.equals(new Role("Write tests")));
        assertFalse(role.equals(null));
        assertFalse(role.equals("Review tests"));
    }
}
