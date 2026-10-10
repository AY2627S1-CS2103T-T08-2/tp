package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class ProjectIdTest {

    private static final String VALID_ID =
            "00000000-0000-4000-8000-000000000001";
    private static final String OTHER_ID =
            "00000000-0000-4000-8000-000000000002";

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ProjectId(null));
    }

    @Test
    public void constructor_validUuid_preservesValue() {
        UUID value = UUID.fromString(VALID_ID);

        assertEquals(value, new ProjectId(value).value);
    }

    @Test
    public void generate_returnsUuid() {
        ProjectId id = ProjectId.generate();

        assertNotNull(id);
        assertNotNull(id.value);
        assertEquals(4, id.value.version());
    }

    @Test
    public void fromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ProjectId.fromString(null));
    }

    @Test
    public void fromString_invalidValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ProjectId.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> ProjectId.fromString("not-a-uuid"));
    }

    @Test
    public void fromString_validValue_returnsExpectedId() {
        assertEquals(
                new ProjectId(UUID.fromString(VALID_ID)),
                ProjectId.fromString(VALID_ID));
    }

    @Test
    public void toString_roundTrip_preservesId() {
        ProjectId original = ProjectId.fromString(VALID_ID);

        assertEquals(VALID_ID, original.toString());
        assertEquals(original, ProjectId.fromString(original.toString()));
    }

    @Test
    public void equals() {
        ProjectId id = ProjectId.fromString(VALID_ID);
        ProjectId copy = ProjectId.fromString(VALID_ID);

        assertTrue(id.equals(id));
        assertTrue(id.equals(copy));
        assertTrue(copy.equals(id));
        assertEquals(id.hashCode(), copy.hashCode());

        assertFalse(id.equals(ProjectId.fromString(OTHER_ID)));
        assertFalse(id.equals(null));
        assertFalse(id.equals(VALID_ID));
    }
}
