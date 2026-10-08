package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ProjectNameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ProjectName(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new ProjectName(invalidName));
    }

    @Test
    public void constructor_normalisesWhitespace_preservesCapitalisation() {
        ProjectName name = new ProjectName("  CS2103T   Team   Project  ");
        assertEquals("CS2103T Team Project", name.toString());
    }

    @Test
    public void equals_differentCaseAndSpacing_returnsTrue() {
        ProjectName name = new ProjectName("CS2103T Team Project");
        ProjectName equivalent = new ProjectName("  cs2103t   team project  ");

        assertEquals(name, equivalent);
        assertEquals(equivalent, name);
        assertEquals(name.hashCode(), equivalent.hashCode());
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> ProjectName.isValidName(null));

        // invalid name
        assertFalse(ProjectName.isValidName("")); // empty string
        assertFalse(ProjectName.isValidName(" ")); // spaces only
        assertFalse(ProjectName.isValidName("^")); // only non-alphanumeric characters
        assertFalse(ProjectName.isValidName("peter*")); // contains non-alphanumeric characters
        assertFalse(ProjectName.isValidName("a".repeat(51)));
        assertFalse(ProjectName.isValidName("-'&()"));

        // valid name
        assertTrue(ProjectName.isValidName("peter jack")); // alphabets only
        assertTrue(ProjectName.isValidName("12345")); // numbers only
        assertTrue(ProjectName.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(ProjectName.isValidName("Capital Tan")); // with capital letters
        assertTrue(ProjectName.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(ProjectName.isValidName("a".repeat(50)));
        assertTrue(ProjectName.isValidName("Team-A's Project & Research (2026)")); // allowed punctuation
    }

    @Test
    public void equals() {
        ProjectName name = new ProjectName("Valid Project Name");

        // same values -> returns true
        assertTrue(name.equals(new ProjectName("Valid Project Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new ProjectName("Other Valid Project Name")));
    }
}
