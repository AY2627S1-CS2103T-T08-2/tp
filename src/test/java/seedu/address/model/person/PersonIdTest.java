package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class PersonIdTest {

    private static final String VALID_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonId(null));
    }

    @Test
    public void constructor_validUuid_preservesUuid() {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        PersonId personId = new PersonId(id);

        assertEquals(id, personId.getId());
    }

    @Test
    public void generate_calledTwice_returnsDistinctNonNullIds() {
        PersonId firstId = PersonId.generate();
        PersonId secondId = PersonId.generate();

        assertNotNull(firstId.getId());
        assertNotNull(secondId.getId());
        assertNotEquals(firstId, secondId);
    }

    @Test
    public void fromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PersonId.fromString(null));
    }

    @Test
    public void fromString_validUuid_preservesUuid() {
        PersonId personId = PersonId.fromString(VALID_ID);

        assertEquals(UUID.fromString(VALID_ID), personId.getId());
        assertEquals(VALID_ID, personId.toString());
    }

    @Test
    public void fromString_uppercaseUuid_returnsCanonicalUuid() {
        PersonId personId = PersonId.fromString(VALID_ID.toUpperCase(Locale.ROOT));

        assertEquals(VALID_ID, personId.toString());
        assertEquals(PersonId.fromString(VALID_ID), personId);
    }

    @Test
    public void fromString_invalidUuid_throwsIllegalArgumentException() {
        String[] invalidIds = {
            "", " ", "not-a-uuid", "1-1-1-1-1",
            "550e8400-e29b-41d4-a716-44665544000g",
            "550e8400-e29b-41d4-a716-446655440000 ",
            "0550e8400-e29b-41d4-a716-446655440000"
        };

        for (String invalidId : invalidIds) {
            assertThrows(IllegalArgumentException.class, () -> PersonId.fromString(invalidId));
        }
    }
}
