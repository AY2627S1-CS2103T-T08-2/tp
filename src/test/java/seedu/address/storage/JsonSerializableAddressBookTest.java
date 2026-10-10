package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.PersonId;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");
    private static final Path VALID_PERSON_IDS_FILE = TEST_DATA_FOLDER.resolve("validPersonIdsAddressBook.json");
    private static final Path DUPLICATE_PERSON_IDS_FILE =
            TEST_DATA_FOLDER.resolve("duplicatePersonIdsAddressBook.json");
    private static final Path DUPLICATE_PERSON_IDS_DIFFERENT_CASE_FILE =
            TEST_DATA_FOLDER.resolve("duplicatePersonIdsDifferentCaseAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_validPersonIds_preservesIds() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(VALID_PERSON_IDS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();

        assertEquals(2, addressBookFromFile.getPersonList().size());
        assertEquals(PersonId.fromString("550e8400-e29b-41d4-a716-446655440001"),
                addressBookFromFile.getPersonList().get(0).getPersonId());
        assertEquals(PersonId.fromString("550e8400-e29b-41d4-a716-446655440002"),
                addressBookFromFile.getPersonList().get(1).getPersonId());
    }

    @Test
    public void toModelType_duplicatePersonIds_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_IDS_FILE,
                JsonSerializableAddressBook.class).get();

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON_ID,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersonIdsDifferentCase_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_IDS_DIFFERENT_CASE_FILE,
                JsonSerializableAddressBook.class).get();

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON_ID,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateNamesAndIds_reportsDuplicatePerson() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(
                new JsonAdaptedPerson(TypicalPersons.ALICE), new JsonAdaptedPerson(TypicalPersons.ALICE)));

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                data::toModelType);
    }

}
