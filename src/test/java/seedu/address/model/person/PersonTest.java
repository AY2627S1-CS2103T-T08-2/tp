package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    private static final PersonId PERSON_ID = PersonId.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final PersonId OTHER_PERSON_ID = PersonId.fromString("550e8400-e29b-41d4-a716-446655440001");

    @Test
    public void constructor_nullDetails_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Person(PERSON_ID, null, ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), ALICE.getTags()));
        assertThrows(NullPointerException.class, () -> new Person(PERSON_ID, ALICE.getName(), null,
                ALICE.getEmail(), ALICE.getAddress(), ALICE.getTags()));
        assertThrows(NullPointerException.class, () -> new Person(PERSON_ID, ALICE.getName(), ALICE.getPhone(),
                null, ALICE.getAddress(), ALICE.getTags()));
        assertThrows(NullPointerException.class, () -> new Person(PERSON_ID, ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), null, ALICE.getTags()));
        assertThrows(NullPointerException.class, () -> new Person(PERSON_ID, ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), null));
    }

    @Test
    public void constructor_nullTagElement_throwsNullPointerException() {
        Set<Tag> tags = new HashSet<>();
        tags.add(null);
        assertThrows(NullPointerException.class, () -> new Person(PERSON_ID, ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), tags));
    }

    @Test
    public void constructor_mutatingSuppliedTags_doesNotChangePerson() {
        Set<Tag> tags = new HashSet<>();
        Tag friend = new Tag("friend");
        tags.add(friend);
        Person person = new Person(PERSON_ID, ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), tags);
        int originalHashCode = person.hashCode();

        tags.clear();
        tags.add(new Tag("colleague"));

        assertEquals(Set.of(friend), person.getTags());
        assertEquals(originalHashCode, person.hashCode());
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().add(new Tag("other")));
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(friend));
    }

    @Test
    public void equals_emptyAndReorderedTags_preservesSetSemantics() {
        Person noTags = new PersonBuilder(ALICE).withTags().build();
        Person noTagsCopy = new PersonBuilder(noTags).withPersonId(OTHER_PERSON_ID).build();
        assertTrue(noTags.getTags().isEmpty());
        assertEquals(noTags, noTagsCopy);
        assertEquals(noTags.hashCode(), noTagsCopy.hashCode());

        Person person = new PersonBuilder(ALICE).withTags("friend", "colleague").build();
        Person reordered = new PersonBuilder(person).withTags("colleague", "friend").build();
        assertEquals(person, reordered);
        assertEquals(person.hashCode(), reordered.hashCode());
        assertNotEquals(noTags, person);
    }

    @Test
    public void constructor_withoutId_generatesDistinctNonNullIds() {
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), ALICE.getTags());
        Person otherPerson = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), ALICE.getTags());

        assertNotNull(person.getPersonId());
        assertNotNull(otherPerson.getPersonId());
        assertNotEquals(person.getPersonId(), otherPerson.getPersonId());
    }

    @Test
    public void constructor_withId_preservesSuppliedId() {
        Person person = new Person(PERSON_ID, ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), ALICE.getTags());

        assertEquals(PERSON_ID, person.getPersonId());
        assertEquals(ALICE, person);
    }

    @Test
    public void constructor_nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Person(null, ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), ALICE.getTags()));
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, all other attributes different -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns false
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void isSamePerson_ignoresIds_comparesContactDetails() {
        Person person = new PersonBuilder(ALICE).withPersonId(PERSON_ID).build();
        Person editedAlice = new PersonBuilder(person).withPersonId(OTHER_PERSON_ID).build();
        Person renamedAlice = new PersonBuilder(person).withName(VALID_NAME_BOB).build();

        assertNotEquals(person.getPersonId(), editedAlice.getPersonId());
        assertTrue(person.isSamePerson(editedAlice));
        assertEquals(person.getPersonId(), renamedAlice.getPersonId());
        assertFalse(person.isSamePerson(renamedAlice));
    }

    @Test
    public void isSamePerson_oneFieldDiffers_returnsFalse() {
        assertFalse(ALICE.isSamePerson(new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build()));
        assertFalse(ALICE.isSamePerson(new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build()));
        assertFalse(ALICE.isSamePerson(new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build()));
        assertFalse(ALICE.isSamePerson(new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build()));
        assertFalse(ALICE.isSamePerson(new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build()));
    }

    @Test
    public void isSamePerson_tagsInDifferentOrder_returnsTrue() {
        Person person = new PersonBuilder(ALICE).withTags("friends", "classmates").build();
        Person otherPerson = new PersonBuilder(person).withPersonId(OTHER_PERSON_ID)
                .withTags("classmates", "friends").build();

        assertTrue(person.isSamePerson(otherPerson));
        assertTrue(otherPerson.isSamePerson(person));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void equals_differentIdsWithSameDetails_returnsTrueWithMatchingHashCodes() {
        Person person = new PersonBuilder(ALICE).withPersonId(PERSON_ID).build();
        Person aliceCopy = new PersonBuilder(person).withPersonId(OTHER_PERSON_ID).build();

        assertNotEquals(person.getPersonId(), aliceCopy.getPersonId());
        assertEquals(person, aliceCopy);
        assertEquals(aliceCopy, person);
        assertEquals(person.hashCode(), aliceCopy.hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress() + ", tags=" + ALICE.getTags() + "}";
        assertEquals(expected, ALICE.toString());
        Person aliceCopy = new PersonBuilder(ALICE).withPersonId(OTHER_PERSON_ID).build();
        assertEquals(expected, aliceCopy.toString());
    }
}
