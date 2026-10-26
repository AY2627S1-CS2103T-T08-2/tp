package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.ContactDuplicateTestData.ALL_FIELDS;
import static seedu.address.testutil.ContactDuplicateTestData.BASE;
import static seedu.address.testutil.ContactDuplicateTestData.FIRST_ID;
import static seedu.address.testutil.ContactDuplicateTestData.SECOND_ID;
import static seedu.address.testutil.ContactDuplicateTestData.person;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/** Tests all equal/different partitions of the five contact-detail fields. */
public class PersonDuplicatePolicyTest {
    @TestFactory
    public Stream<DynamicTest> equals_allFieldAndIdentifierPartitions_matchesOnlyCompleteDetails() {
        return IntStream.rangeClosed(0, ALL_FIELDS).boxed().flatMap(differences ->
                IntStream.range(0, 2).mapToObj(sameId ->
                        dynamicTest("different fields " + differences + " same ID " + sameId, () -> {
                            Person candidate = person(differences, sameId == 1 ? FIRST_ID : SECOND_ID);
                            boolean duplicate = differences == 0;

                            assertEquals(duplicate, BASE.equals(candidate));
                            assertEquals(duplicate, candidate.equals(BASE));
                            assertEquals(duplicate, BASE.isSamePerson(candidate));
                            assertEquals(duplicate, candidate.isSamePerson(BASE));
                            if (duplicate) {
                                assertEquals(BASE.hashCode(), candidate.hashCode());
                                assertEquals(1, new HashSet<>(List.of(BASE, candidate)).size());
                            }
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> equals_tagSetPartitions_comparesMembershipRatherThanOrder() {
        List<Set<Tag>> tagSets = List.of(Set.of(), Set.of(new Tag("friend")),
                new LinkedHashSet<>(List.of(new Tag("friend"), new Tag("classmate"))),
                new LinkedHashSet<>(List.of(new Tag("classmate"), new Tag("friend"))),
                Set.of(new Tag("friend"), new Tag("colleague")),
                Set.of(new Tag("friend"), new Tag("classmate"), new Tag("colleague")),
                Set.of(new Tag("Friend"), new Tag("classmate")));
        return IntStream.range(0, tagSets.size()).mapToObj(partition -> dynamicTest("tags " + partition, () -> {
            Person candidate = new Person(SECOND_ID, BASE.getName(), BASE.getPhone(), BASE.getEmail(),
                    BASE.getAddress(), tagSets.get(partition));
            boolean duplicate = partition == 2 || partition == 3;

            assertEquals(duplicate, BASE.isSamePerson(candidate));
            assertEquals(duplicate, candidate.isSamePerson(BASE));
            if (duplicate) {
                assertEquals(BASE.hashCode(), candidate.hashCode());
            }
        }));
    }

    @Test
    public void equals_exactFieldValues_preservesCaseAndWhitespaceSemantics() {
        List<Person> distinct = List.of(new PersonBuilder(BASE).withName("alex Tan").build(),
                new PersonBuilder(BASE).withName("Alex  Tan").build(),
                new PersonBuilder(BASE).withName("Alex Tan ").build(),
                new PersonBuilder(BASE).withPhone("0123").build(),
                new PersonBuilder(BASE).withEmail("Alex@example.com").build(),
                new PersonBuilder(BASE).withAddress("Block 1 ").build());
        for (Person candidate : distinct) {
            assertFalse(BASE.isSamePerson(candidate));
            assertFalse(candidate.isSamePerson(BASE));
        }
        assertFalse(BASE.isSamePerson(null));
        assertFalse(BASE.equals(BASE.getName()));
    }
}
