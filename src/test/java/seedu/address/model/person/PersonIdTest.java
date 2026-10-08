package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class PersonIdTest {

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
}
