package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.model.tag.Tag;

/** Tests identifier generation and reconstruction through both Person constructors. */
public class PersonIdentityTest {
    @TestFactory
    public Stream<DynamicTest> constructor_explicitIdAndNullArguments_rejectsNull() {
        return IntStream.range(0, 6).mapToObj(argument -> dynamicTest("explicit null argument " + argument, () -> {
            assertThrows(NullPointerException.class, () -> new Person(
                    argument == 0 ? null : ALICE.getPersonId(),
                    argument == 1 ? null : ALICE.getName(),
                    argument == 2 ? null : ALICE.getPhone(),
                    argument == 3 ? null : ALICE.getEmail(),
                    argument == 4 ? null : ALICE.getAddress(),
                    argument == 5 ? null : ALICE.getTags()));
        }));
    }

    @TestFactory
    public Stream<DynamicTest> constructor_generatedIdAndNullArguments_rejectsNull() {
        return IntStream.range(0, 5).mapToObj(argument -> dynamicTest("generating null argument " + argument, () -> {
            assertThrows(NullPointerException.class, () -> new Person(
                    argument == 0 ? null : ALICE.getName(),
                    argument == 1 ? null : ALICE.getPhone(),
                    argument == 2 ? null : ALICE.getEmail(),
                    argument == 3 ? null : ALICE.getAddress(),
                    argument == 4 ? null : ALICE.getTags()));
        }));
    }

    @TestFactory
    public Stream<DynamicTest> constructor_explicitBitBoundariesAndTagCounts_preservesId() {
        List<PersonId> ids = List.of(new PersonId(new UUID(0, 0)), ALICE.getPersonId(),
                new PersonId(new UUID(-1, -1)));
        List<Set<Tag>> tagSets = List.of(Set.of(), Set.of(new Tag("friend")),
                Set.of(new Tag("friend"), new Tag("colleague")));
        return IntStream.range(0, ids.size()).boxed().flatMap(idIndex ->
                IntStream.range(0, tagSets.size()).mapToObj(tagIndex ->
                        dynamicTest("identifier " + idIndex + " tag count " + tagIndex, () -> {
                            PersonId id = ids.get(idIndex);
                            Person person = new Person(id, ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                                    ALICE.getAddress(), tagSets.get(tagIndex));
                            assertEquals(id, person.getPersonId());
                            assertEquals(id.getId(), person.getPersonId().getId());
                            assertEquals(id.toString(), person.getPersonId().toString());
                            assertEquals(tagSets.get(tagIndex), person.getTags());
                        })));
    }

    @Test
    public void constructor_identicalDetails_generatesDistinctIdsAndRestoresSuppliedId() {
        Set<PersonId> ids = new HashSet<>();
        for (int index = 0; index < 64; index++) {
            Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                    ALICE.getAddress(), ALICE.getTags());
            Person restored = new Person(PersonId.fromString(person.getPersonId().toString()), person.getName(),
                    person.getPhone(), person.getEmail(), person.getAddress(), person.getTags());
            assertTrue(ids.add(person.getPersonId()), "New contacts must receive distinct identifiers");
            assertEquals(person.getPersonId(), restored.getPersonId());
            assertEquals(person.getPersonId().hashCode(), restored.getPersonId().hashCode());
        }
    }
}
