package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("\t ")); // whitespace only
        assertFalse(Phone.isValidPhone("1234\n5678")); // line breaks
        assertFalse(Phone.isValidPhone("1234\u007F5678")); // control characters

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911"));
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("+65 9123 4567"));
        assertTrue(Phone.isValidPhone("9123-4567"));
        assertTrue(Phone.isValidPhone("1234 5678 (HP) 1111-3333 (Office)"));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }

    @Test
    public void isValidPhone_unicodeWhitespaceOnly_returnsFalse() {
        String[] blankValues = {"\u2002", "\u2003", "\u00A0", "\u2007", "\u202F", "\u3000", " \u2003\u00A0\u3000 "};
        for (String value : blankValues) {
            assertFalse(Phone.isValidPhone(value));
            assertThrows(IllegalArgumentException.class, () -> new Phone(value));
        }
    }

    @Test
    public void constructor_unicodeWhitespaceOnly_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Phone("\u2003"));
    }

    @Test
    public void isValidPhone_longInputWithTrailingControlCharacter_returnsFalsePromptly() {
        String invalidPhone = "1".repeat(100_000) + "\u007F";

        assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                assertFalse(Phone.isValidPhone(invalidPhone)));
    }

    @Test
    public void isValidPhone_unicodeSpaceWithinText_returnsTrue() {
        assertTrue(Phone.isValidPhone("+65\u00A09123\u202F4567 (HP)"));
    }
}
