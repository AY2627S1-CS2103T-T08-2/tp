package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.PersonBuilder;

public class UniquePersonListTest {

    private final UniquePersonList uniquePersonList = new UniquePersonList();

    @Test
    public void contains_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.contains(null));
    }

    @Test
    public void contains_personNotInList_returnsFalse() {
        assertFalse(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        assertTrue(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_sameNameWithDifferentDetails_returnsFalse() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertFalse(uniquePersonList.contains(editedAlice));
    }

    @Test
    public void contains_matchingDetailsWithDifferentId_returnsTrue() {
        uniquePersonList.add(ALICE);
        Person duplicate = new PersonBuilder(ALICE).withPersonId(PersonId.generate()).build();

        assertTrue(uniquePersonList.contains(duplicate));
    }

    @Test
    public void add_sameNameWithDifferentDetails_success() {
        Person otherAlice = new PersonBuilder(ALICE).withPersonId(PersonId.generate())
                .withAddress(VALID_ADDRESS_BOB).build();
        uniquePersonList.add(ALICE);

        uniquePersonList.add(otherAlice);

        assertEquals(List.of(ALICE, otherAlice), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void add_matchingDetailsWithDifferentId_throwsDuplicatePersonException() {
        Person duplicate = new PersonBuilder(ALICE).withPersonId(PersonId.generate()).build();
        uniquePersonList.add(ALICE);

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(duplicate));
        assertEquals(List.of(ALICE), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void setPerson_sameNameWithDifferentDetails_success() {
        uniquePersonList.setPersons(List.of(ALICE, BOB));
        Person editedBob = new PersonBuilder(BOB).withName(ALICE.getName().fullName).build();

        uniquePersonList.setPerson(BOB, editedBob);

        assertEquals(List.of(ALICE, editedBob), uniquePersonList.asUnmodifiableObservableList());
        assertEquals(BOB.getPersonId(), uniquePersonList.asUnmodifiableObservableList().get(1).getPersonId());
    }

    @Test
    public void setPerson_sameNameBecomesExactDuplicate_throwsDuplicatePersonException() {
        Person otherAlice = new PersonBuilder(ALICE).withPersonId(PersonId.generate())
                .withAddress(VALID_ADDRESS_BOB).build();
        uniquePersonList.setPersons(List.of(ALICE, otherAlice));
        Person duplicate = new PersonBuilder(ALICE).withPersonId(otherAlice.getPersonId()).build();

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(otherAlice, duplicate));
        assertEquals(List.of(ALICE, otherAlice), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void setPersons_sameNameWithDifferentDetails_success() {
        Person otherAlice = new PersonBuilder(ALICE).withPersonId(PersonId.generate())
                .withAddress(VALID_ADDRESS_BOB).build();

        uniquePersonList.setPersons(List.of(ALICE, otherAlice));

        assertEquals(List.of(ALICE, otherAlice), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void setPersons_matchingDetailsWithDifferentIds_throwsDuplicatePersonException() {
        uniquePersonList.add(BOB);
        Person duplicate = new PersonBuilder(ALICE).withPersonId(PersonId.generate()).build();

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(List.of(ALICE, duplicate)));
        assertEquals(List.of(BOB), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void containsId_nullPersonId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.containsId(null));
    }

    @Test
    public void containsId_personIdNotInList_returnsFalse() {
        assertFalse(uniquePersonList.containsId(ALICE.getPersonId()));
    }

    @Test
    public void containsId_personIdInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        assertTrue(uniquePersonList.containsId(ALICE.getPersonId()));
    }

    @Test
    public void add_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.add(null));
    }

    @Test
    public void add_duplicatePerson_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(ALICE));
    }

    @Test
    public void add_duplicatePersonId_throwsDuplicatePersonException() {
        Person personWithDuplicateId = new PersonBuilder(BOB).withPersonId(ALICE.getPersonId()).build();
        uniquePersonList.add(ALICE);

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(personWithDuplicateId));
    }

    @Test
    public void setPerson_nullTargetPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(null, ALICE));
    }

    @Test
    public void setPerson_nullEditedPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(ALICE, null));
    }

    @Test
    public void setPerson_targetPersonNotInList_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.setPerson(ALICE, ALICE));
    }

    @Test
    public void setPerson_editedPersonIsSamePerson_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(ALICE);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasSameIdentity_success() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        uniquePersonList.setPerson(ALICE, editedAlice);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(editedAlice);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasDifferentId_throwsIllegalArgumentException() {
        uniquePersonList.add(ALICE);
        Person personWithDifferentId = new PersonBuilder(ALICE).withPersonId(BOB.getPersonId()).build();

        assertThrows(IllegalArgumentException.class, () -> uniquePersonList.setPerson(ALICE, personWithDifferentId));
        assertEquals(List.of(ALICE), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void setPerson_equivalentTargetClaimsDifferentId_rejectsWithoutMutation() {
        List<Person> people = List.of(ALICE, BOB, CARL);
        for (int targetIndex = 0; targetIndex < people.size(); targetIndex++) {
            for (Person claimedIdOwner : people) {
                if (claimedIdOwner == people.get(targetIndex)) {
                    continue;
                }
                for (boolean changeDetails : List.of(false, true)) {
                    uniquePersonList.setPersons(people);
                    Person storedPerson = people.get(targetIndex);
                    Person target = new PersonBuilder(storedPerson).withPersonId(claimedIdOwner.getPersonId()).build();
                    PersonBuilder replacement = new PersonBuilder(target);
                    if (changeDetails) {
                        replacement.withAddress(VALID_ADDRESS_BOB);
                    }

                    assertThrows(IllegalArgumentException.class,
                            () -> uniquePersonList.setPerson(target, replacement.build()));

                    assertEquals(people, uniquePersonList.asUnmodifiableObservableList());
                    assertEquals(people.stream().map(Person::getPersonId).toList(),
                            uniquePersonList.asUnmodifiableObservableList().stream()
                                    .map(Person::getPersonId).toList());
                }
            }

            uniquePersonList.setPersons(people);
            Person storedPerson = people.get(targetIndex);
            Person target = new PersonBuilder(storedPerson).withPersonId(PersonId.generate()).build();
            assertThrows(IllegalArgumentException.class, () -> uniquePersonList.setPerson(target, target));
            assertEquals(people.stream().map(Person::getPersonId).toList(),
                    uniquePersonList.asUnmodifiableObservableList().stream()
                            .map(Person::getPersonId).toList());
        }
    }

    @Test
    public void setPerson_equivalentTargetWithStoredId_allowsEdit() {
        uniquePersonList.setPersons(List.of(ALICE, BOB));
        Person target = new PersonBuilder(ALICE).build();
        Person editedAlice = new PersonBuilder(target).withAddress(VALID_ADDRESS_BOB).build();

        uniquePersonList.setPerson(target, editedAlice);

        assertEquals(editedAlice, uniquePersonList.asUnmodifiableObservableList().get(0));
        assertEquals(ALICE.getPersonId(), uniquePersonList.asUnmodifiableObservableList().get(0).getPersonId());
        assertEquals(BOB.getPersonId(), uniquePersonList.asUnmodifiableObservableList().get(1).getPersonId());
    }

    @Test
    public void setPerson_editedPersonHasNonUniqueIdentity_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person editedAlice = new PersonBuilder(BOB).withPersonId(ALICE.getPersonId()).build();

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, editedAlice));
    }

    @Test
    public void remove_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.remove(null));
    }

    @Test
    public void remove_personDoesNotExist_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.remove(ALICE));
    }

    @Test
    public void remove_existingPerson_removesPerson() {
        uniquePersonList.add(ALICE);
        uniquePersonList.remove(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullUniquePersonList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((UniquePersonList) null));
    }

    @Test
    public void setPersons_uniquePersonList_replacesOwnListWithProvidedUniquePersonList() {
        uniquePersonList.add(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        uniquePersonList.setPersons(expectedUniquePersonList);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((List<Person>) null));
    }

    @Test
    public void setPersons_list_replacesOwnListWithProvidedList() {
        uniquePersonList.add(ALICE);
        List<Person> personList = List.of(BOB);
        uniquePersonList.setPersons(personList);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_listWithDuplicatePersons_throwsDuplicatePersonException() {
        List<Person> listWithDuplicatePersons = List.of(ALICE, ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(listWithDuplicatePersons));
    }

    @Test
    public void setPersons_listWithDuplicatePersonIds_throwsDuplicatePersonException() {
        Person personWithDuplicateId = new PersonBuilder(BOB).withPersonId(ALICE.getPersonId()).build();
        List<Person> listWithDuplicatePersonIds = List.of(ALICE, personWithDuplicateId);

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(listWithDuplicatePersonIds));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, ()
            -> uniquePersonList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniquePersonList.asUnmodifiableObservableList().toString(), uniquePersonList.toString());
    }
}
