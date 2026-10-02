package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void equals() {
        Remark remark = new Remark("Likes swimming");

        assertEquals(remark, new Remark("Likes swimming"));
        assertNotEquals(remark, new Remark("Likes running"));
        assertNotEquals(remark, null);
    }

    @Test
    public void toString_returnsValue() {
        assertEquals("Likes swimming", new Remark("Likes swimming").toString());
    }
}
