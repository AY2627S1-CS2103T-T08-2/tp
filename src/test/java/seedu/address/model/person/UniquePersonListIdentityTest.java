package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.PersonBuilder;

/** Tests identifier invariants independently of Person.equals, which ignores identifiers. */
public class UniquePersonListIdentityTest {
    private static final PersonId UNUSED_ID = new PersonId(new UUID(-1, -1));
    private static final List<Person> ORIGINALS = List.of(ALICE,
            new PersonBuilder(BOB).withPersonId(new PersonId(new UUID(0, 1))).build(),
            new PersonBuilder().withPersonId(new PersonId(new UUID(0, 0))).withName("Carol").build());

    @TestFactory
    public Stream<DynamicTest> add_duplicateIdAtEachPosition_preservesStoredData() {
        return IntStream.range(0, ORIGINALS.size()).mapToObj(position ->
                dynamicTest("duplicate identifier at position " + position, () -> {
                    UniquePersonList persons = populatedList();
                    Person duplicate = new PersonBuilder().withName("Different Details")
                            .withPersonId(ORIGINALS.get(position).getPersonId()).build();

                    assertThrows(DuplicatePersonException.class, () -> persons.add(duplicate));

                    assertStoredData(ORIGINALS, persons);
                }));
    }

    @TestFactory
    public Stream<DynamicTest> add_identifierAndDetailPartitions_enforcesBothUniquenessRules() {
        return IntStream.range(0, 4).mapToObj(partition -> dynamicTest("add partition " + partition, () -> {
            UniquePersonList persons = new UniquePersonList();
            persons.add(ALICE);
            boolean sameId = partition < 2;
            boolean sameDetails = partition % 2 == 0;
            PersonBuilder builder = new PersonBuilder(ALICE).withPersonId(sameId ? ALICE.getPersonId() : UNUSED_ID);
            Person candidate = (sameDetails ? builder : builder.withName("Different Name")).build();

            if (sameId || sameDetails) {
                assertThrows(DuplicatePersonException.class, () -> persons.add(candidate));
                assertStoredData(List.of(ALICE), persons);
            } else {
                persons.add(candidate);
                assertStoredData(List.of(ALICE, candidate), persons);
            }
        }));
    }

    @TestFactory
    public Stream<DynamicTest> setPerson_storedOrEquivalentTarget_preservesActualId() {
        return IntStream.range(0, ORIGINALS.size()).boxed().flatMap(position ->
                IntStream.range(0, 4).mapToObj(partition ->
                        dynamicTest("valid edit position " + position + " partition " + partition, () -> {
                            UniquePersonList persons = populatedList();
                            Person stored = ORIGINALS.get(position);
                            Person target = partition < 2 ? stored : new PersonBuilder(stored).build();
                            Person edited = partition % 2 == 0 ? target
                                    : new PersonBuilder(target).withName("Edited Name").build();
                            List<Person> expected = new ArrayList<>(ORIGINALS);
                            expected.set(position, edited);

                            persons.setPerson(target, edited);

                            assertStoredData(expected, persons);
                            assertEquals(stored.getPersonId(), persons.asUnmodifiableObservableList()
                                    .get(position).getPersonId());
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> setPerson_replacementChangesId_rejectsExistingAndUnusedIds() {
        return IntStream.range(0, ORIGINALS.size()).boxed().flatMap(position ->
                IntStream.range(0, 2).mapToObj(partition ->
                        dynamicTest("changed ID position " + position + " partition " + partition, () -> {
                            UniquePersonList persons = populatedList();
                            Person stored = ORIGINALS.get(position);
                            PersonId changedId = partition == 0 ? UNUSED_ID
                                    : ORIGINALS.get((position + 1) % ORIGINALS.size()).getPersonId();
                            Person edited = new PersonBuilder(stored).withPersonId(changedId).build();

                            assertThrows(IllegalArgumentException.class, () -> persons.setPerson(stored, edited));

                            assertStoredData(ORIGINALS, persons);
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> setPerson_equivalentTargetClaimsDifferentId_rejectsIdCorruption() {
        return IntStream.range(0, ORIGINALS.size()).boxed().flatMap(position ->
                IntStream.range(0, 4).mapToObj(partition ->
                        dynamicTest("mismatched target position " + position + " partition " + partition, () -> {
                            UniquePersonList persons = populatedList();
                            PersonId claimedId = partition < 2 ? UNUSED_ID
                                    : ORIGINALS.get((position + 1) % ORIGINALS.size()).getPersonId();
                            Person target = new PersonBuilder(ORIGINALS.get(position)).withPersonId(claimedId).build();
                            Person edited = partition % 2 == 0 ? target
                                    : new PersonBuilder(target).withName("Edited Name").build();

                            assertAll(() -> {
                                assertThrows(IllegalArgumentException.class, () -> persons.setPerson(target, edited));
                            }, () -> {
                                assertStoredData(ORIGINALS, persons);
                            });
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> setPerson_staleDetailsWithStoredId_preservesData() {
        return IntStream.range(0, ORIGINALS.size()).mapToObj(position ->
                dynamicTest("stale target position " + position, () -> {
                    UniquePersonList persons = populatedList();
                    Person target = new PersonBuilder(ORIGINALS.get(position)).withName("Stale Details").build();

                    assertThrows(PersonNotFoundException.class, () -> persons.setPerson(target, target));

                    assertStoredData(ORIGINALS, persons);
                }));
    }

    @TestFactory
    public Stream<DynamicTest> setPersons_duplicateIdPairs_rejectsBeforeMutation() {
        return IntStream.range(0, ORIGINALS.size()).boxed().flatMap(first ->
                IntStream.range(first + 1, ORIGINALS.size()).mapToObj(second ->
                        dynamicTest("duplicate ID positions " + first + " and " + second, () -> {
                            UniquePersonList persons = populatedList();
                            List<Person> replacement = new ArrayList<>(ORIGINALS);
                            replacement.set(second, new PersonBuilder(ORIGINALS.get(second))
                                    .withPersonId(ORIGINALS.get(first).getPersonId()).build());

                            assertThrows(DuplicatePersonException.class, () -> persons.setPersons(replacement));

                            assertStoredData(ORIGINALS, persons);
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> setPersons_nullAtEachPosition_rejectsBeforeMutation() {
        return IntStream.range(0, ORIGINALS.size()).mapToObj(position ->
                dynamicTest("null at position " + position, () -> {
                    UniquePersonList persons = populatedList();
                    List<Person> replacement = new ArrayList<>(ORIGINALS);
                    replacement.set(position, null);

                    assertThrows(NullPointerException.class, () -> persons.setPersons(replacement));

                    assertStoredData(ORIGINALS, persons);
                }));
    }

    @TestFactory
    public Stream<DynamicTest> setPersons_zeroOneAndManyContacts_preservesSuppliedIdsAndCopiesList() {
        return IntStream.of(0, 1, 2, 3).mapToObj(size -> dynamicTest("replacement size " + size, () -> {
            UniquePersonList persons = populatedList();
            List<Person> expected = ORIGINALS.subList(0, size);
            List<Person> replacement = new ArrayList<>(expected);

            persons.setPersons(replacement);
            replacement.clear();

            assertStoredData(expected, persons);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> setPersons_selfOrOwnView_preservesIdentifiers() {
        return IntStream.range(0, 2).mapToObj(partition -> dynamicTest("aliased replacement " + partition, () -> {
            UniquePersonList persons = populatedList();

            if (partition == 0) {
                persons.setPersons(persons);
            } else {
                persons.setPersons(persons.asUnmodifiableObservableList());
            }

            assertStoredData(ORIGINALS, persons);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> setPersons_uniqueListReplacement_preservesIdsAndCopiesCollection() {
        return IntStream.of(0, 1, 2, 3).mapToObj(size -> dynamicTest("unique list replacement size " + size, () -> {
            UniquePersonList persons = populatedList();
            UniquePersonList replacement = new UniquePersonList();
            List<Person> expected = ORIGINALS.subList(0, size);
            replacement.setPersons(expected);

            persons.setPersons(replacement);
            replacement.setPersons(List.of());

            assertStoredData(expected, persons);
        }));
    }

    @Test
    public void setPersons_singleNullElement_rejectsBeforeMutation() {
        UniquePersonList persons = populatedList();

        assertThrows(NullPointerException.class, () -> persons.setPersons(Arrays.asList((Person) null)));

        assertStoredData(ORIGINALS, persons);
    }

    @Test
    public void containsId_emptyPopulatedAndMissingPartitions_usesIdRatherThanDetails() {
        UniquePersonList persons = new UniquePersonList();
        assertFalse(persons.containsId(ALICE.getPersonId()));
        persons.setPersons(ORIGINALS);
        for (Person person : ORIGINALS) {
            assertTrue(persons.containsId(PersonId.fromString(person.getPersonId().toString())));
        }
        assertFalse(persons.containsId(UNUSED_ID));
        assertThrows(NullPointerException.class, () -> persons.containsId(null));
        assertStoredData(ORIGINALS, persons);
    }

    private UniquePersonList populatedList() {
        UniquePersonList persons = new UniquePersonList();
        persons.setPersons(ORIGINALS);
        return persons;
    }

    private void assertStoredData(List<Person> expected, UniquePersonList actual) {
        // Person equality deliberately ignores IDs, so both comparisons are required.
        assertEquals(expected.stream().map(Person::getPersonId).toList(),
                actual.asUnmodifiableObservableList().stream().map(Person::getPersonId).toList());
        assertEquals(expected, actual.asUnmodifiableObservableList());
    }
}
