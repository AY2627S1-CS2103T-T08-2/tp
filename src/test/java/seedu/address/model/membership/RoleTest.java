package seedu.address.model.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests for {@link Role}. */
public class RoleTest {

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
