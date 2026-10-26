package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.ContactDuplicateTestData.ALL_FIELDS;
import static seedu.address.testutil.ContactDuplicateTestData.BASE;
import static seedu.address.testutil.ContactDuplicateTestData.SECOND_ID;
import static seedu.address.testutil.ContactDuplicateTestData.UNRELATED;
import static seedu.address.testutil.ContactDuplicateTestData.person;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import seedu.address.model.person.exceptions.DuplicatePersonException;

/** Tests duplicate detection and mutation safety in single and bulk collection updates. */
public class UniquePersonListDuplicatePolicyTest {
    @TestFactory
    public Stream<DynamicTest> add_allFieldPartitions_rejectsOnlyExactDuplicates() {
        return IntStream.rangeClosed(0, ALL_FIELDS).mapToObj(differences ->
                dynamicTest("add different fields " + differences, () -> {
                    UniquePersonList persons = new UniquePersonList();
                    persons.setPersons(List.of(UNRELATED, BASE));
                    Person candidate = person(differences, SECOND_ID);

                    assertEquals(differences == 0, persons.contains(candidate));
                    if (differences == 0) {
                        assertThrows(DuplicatePersonException.class, () -> persons.add(candidate));
                        assertState(List.of(UNRELATED, BASE), persons);
                    } else {
                        persons.add(candidate);
                        assertState(List.of(UNRELATED, BASE, candidate), persons);
                    }
                }));
    }

    @TestFactory
    public Stream<DynamicTest> setPerson_allFieldPartitions_preservesIdsAndRejectsOnlyDuplicates() {
        return IntStream.rangeClosed(0, ALL_FIELDS).boxed().flatMap(differences ->
                IntStream.range(0, 2).mapToObj(order ->
                        dynamicTest("replacement fields " + differences + " order " + order, () -> {
                            Person target = person(ALL_FIELDS, SECOND_ID);
                            List<Person> before = order == 0 ? List.of(target, UNRELATED, BASE)
                                    : List.of(BASE, UNRELATED, target);
                            UniquePersonList persons = new UniquePersonList();
                            persons.setPersons(before);
                            Person edited = person(differences, SECOND_ID);

                            if (differences == 0) {
                                assertThrows(DuplicatePersonException.class, () -> persons.setPerson(target, edited));
                                assertState(before, persons);
                            } else {
                                persons.setPerson(target, edited);
                                List<Person> expected = new ArrayList<>(before);
                                expected.set(order == 0 ? 0 : 2, edited);
                                assertState(expected, persons);
                            }
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> setPersons_allFieldPartitions_checksDuplicatesInEveryPairPosition() {
        return IntStream.rangeClosed(0, ALL_FIELDS).boxed().flatMap(differences ->
                IntStream.range(0, 3).mapToObj(position ->
                        dynamicTest("bulk fields " + differences + " unrelated position " + position, () -> {
                            UniquePersonList persons = new UniquePersonList();
                            persons.add(UNRELATED);
                            List<Person> replacement = new ArrayList<>(List.of(BASE, person(differences, SECOND_ID)));
                            replacement.add(position, UNRELATED);

                            if (differences == 0) {
                                assertThrows(DuplicatePersonException.class, () -> persons.setPersons(replacement));
                                assertState(List.of(UNRELATED), persons);
                            } else {
                                persons.setPersons(replacement);
                                assertState(replacement, persons);
                            }
                        })));
    }

    private void assertState(List<Person> expected, UniquePersonList actual) {
        assertEquals(expected, actual.asUnmodifiableObservableList());
        assertEquals(expected.stream().map(Person::getPersonId).toList(),
                actual.asUnmodifiableObservableList().stream().map(Person::getPersonId).toList());
    }
}
