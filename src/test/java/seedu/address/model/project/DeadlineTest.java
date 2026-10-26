package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class DeadlineTest {

    @Test
    public void constructor_dateBoundaries_preservesDate() {
        LocalDate[] dates = {LocalDate.MIN, LocalDate.MAX, LocalDate.of(2000, 2, 29),
            LocalDate.of(2024, 2, 28), LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 1),
            LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 1)};
        // Calendar parsing and invalid dates belong to the parser; Deadline accepts any non-null LocalDate.
        for (LocalDate date : dates) {
            Deadline deadline = new Deadline(date);
            assertEquals(date, deadline.value);
            assertEquals(date.toString(), deadline.toString());
            assertEquals(deadline.hashCode(), new Deadline(date).hashCode());
        }
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Deadline(null));
    }

    @Test
    public void equals() {
        Deadline deadline = new Deadline(LocalDate.of(2020, 1, 1));

        // same values -> returns true
        assertTrue(deadline.equals(new Deadline(LocalDate.of(2020, 1, 1))));

        // same object -> returns true
        assertTrue(deadline.equals(deadline));

        // null -> returns false
        assertFalse(deadline.equals(null));

        // different types -> returns false
        assertFalse(deadline.equals(5.0f));

        // different values -> returns false
        assertFalse(deadline.equals(new Deadline(LocalDate.of(2021, 1, 1))));
    }
}
