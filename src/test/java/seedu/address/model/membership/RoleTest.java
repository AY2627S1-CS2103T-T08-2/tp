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
    }

    @Test
    public void isValidRole_invalidDescription_returnsFalse() {
        assertFalse(Role.isValidRole("line one\nline two"));
        assertFalse(Role.isValidRole("x".repeat(201)));
    }

    @Test
    public void constructor_invalidRole_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Role("x".repeat(201)));
    }

    @Test
    public void role_sameDescription_equals() {
        assertEquals(new Role("Review tests"), new Role("Review tests"));
    }
}
