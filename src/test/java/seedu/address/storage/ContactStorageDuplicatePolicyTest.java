package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.ContactDuplicateTestData.ALL_FIELDS;
import static seedu.address.testutil.ContactDuplicateTestData.BASE;
import static seedu.address.testutil.ContactDuplicateTestData.SECOND_ID;
import static seedu.address.testutil.ContactDuplicateTestData.person;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.ProjectBuilder;

/** Checks that loading and JSON round trips apply the same five-field duplicate policy. */
public class ContactStorageDuplicatePolicyTest {
    @TestFactory
    public Stream<DynamicTest> toModelType_allFieldAndOrderPartitions_rejectsOnlyExactDuplicates() {
        return IntStream.rangeClosed(0, ALL_FIELDS).boxed().flatMap(differences ->
                IntStream.range(0, 2).mapToObj(order ->
                        dynamicTest("load fields " + differences + " order " + order, () -> {
                            Person candidate = person(differences, SECOND_ID);
                            List<Person> expected = order == 0 ? List.of(BASE, candidate) : List.of(candidate, BASE);
                            JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                                    expected.stream().map(JsonAdaptedPerson::new).toList(), List.of(), List.of());

                            if (differences == 0) {
                                assertThrows(IllegalValueException.class,
                                        JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON, data::toModelType);
                            } else {
                                AddressBook restored = data.toModelType();
                                assertEquals(expected, restored.getPersonList());
                                assertEquals(expected.stream().map(Person::getPersonId).toList(),
                                        restored.getPersonList().stream().map(Person::getPersonId).toList());
                            }
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> jsonRoundTrip_allDistinctFieldPartitions_preservesSeparateContactsAndMemberships() {
        return IntStream.rangeClosed(1, ALL_FIELDS).mapToObj(differences ->
                dynamicTest("round trip different fields " + differences, () -> {
                    Person candidate = person(differences, SECOND_ID);
                    AddressBook original = new AddressBook();
                    original.addPerson(BASE);
                    original.addPerson(candidate);
                    Project project = new ProjectBuilder().build();
                    original.addProject(project);
                    original.addProjectMembership(new ProjectMembership(BASE.getPersonId(), project.getProjectId(),
                            new Role("First task")));
                    original.addProjectMembership(new ProjectMembership(candidate.getPersonId(), project.getProjectId(),
                            new Role("Second task")));

                    String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(original));
                    JsonSerializableAddressBook parsed =
                            JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
                    AddressBook restored = parsed.toModelType();

                    assertEquals(List.of(BASE, candidate), restored.getPersonList());
                    assertEquals(List.of(BASE.getPersonId(), SECOND_ID),
                            restored.getPersonList().stream().map(Person::getPersonId).toList());
                    assertEquals(original.getProjectMembershipList(), restored.getProjectMembershipList());
                    assertEquals(List.of(new Role("First task"), new Role("Second task")),
                            restored.getProjectMembershipList().stream().map(ProjectMembership::getRole).toList());
                    assertEquals(List.of(BASE, candidate), restored.getProjectMembers(project));
                }));
    }

    @TestFactory
    public Stream<DynamicTest> toModelType_tagOrderRepetitionAndCardinality_comparesSets() {
        List<List<String>> tagSets = List.of(List.of(), List.of("friend"), List.of("friend", "classmate"),
                List.of("classmate", "friend"), List.of("friend", "classmate", "friend"),
                List.of("friend", "classmate", "colleague"), List.of("Friend", "classmate"));
        return IntStream.range(0, tagSets.size()).mapToObj(partition ->
                dynamicTest("loaded tags " + partition, () -> {
                    List<JsonAdaptedTag> tags = tagSets.get(partition).stream().map(JsonAdaptedTag::new).toList();
                    JsonAdaptedPerson candidate = new JsonAdaptedPerson(SECOND_ID.toString(), BASE.getName().fullName,
                            BASE.getPhone().value, BASE.getEmail().value, BASE.getAddress().value, tags);
                    JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                            List.of(new JsonAdaptedPerson(BASE), candidate), List.of(), List.of());

                    if (partition >= 2 && partition <= 4) {
                        assertThrows(IllegalValueException.class,
                                JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON, data::toModelType);
                    } else {
                        AddressBook restored = data.toModelType();
                        assertEquals(2, restored.getPersonList().size());
                        assertEquals(List.of(BASE.getPersonId(), SECOND_ID),
                                restored.getPersonList().stream().map(Person::getPersonId).toList());
                    }
                }));
    }

    @TestFactory
    public Stream<DynamicTest> toModelType_nullOrEmptyTags_rejectsDuplicateEmptyTagSets() {
        return IntStream.range(0, 2).mapToObj(partition -> dynamicTest("empty tag representation " + partition, () -> {
            Person first = new PersonBuilder(BASE).withTags().build();
            List<JsonAdaptedTag> tags = partition == 0 ? null : List.of();
            JsonAdaptedPerson candidate = new JsonAdaptedPerson(SECOND_ID.toString(), first.getName().fullName,
                    first.getPhone().value, first.getEmail().value, first.getAddress().value, tags);
            JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                    List.of(new JsonAdaptedPerson(first), candidate), List.of(), List.of());

            assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                    data::toModelType);
        }));
    }
}
