package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");
    private static final Path SERIALIZABLE_TEST_DATA_FOLDER =
            Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonId_throwDataLoadingException() {
        String expectedMessage = IllegalValueException.class.getName() + ": " + PersonId.MESSAGE_CONSTRAINTS;
        assertThrows(DataLoadingException.class, expectedMessage, () -> {
            readAddressBook("invalidPersonIdAddressBook.json");
        });
    }

    @Test
    public void readAddressBook_duplicatePersonIds_throwDataLoadingException() {
        Path filePath = SERIALIZABLE_TEST_DATA_FOLDER.resolve("duplicatePersonIdsAddressBook.json");
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class,
                IllegalValueException.class.getName() + ": " + JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON_ID,
                jsonAddressBookStorage::readAddressBook);
    }

    @Test
    public void readAddressBook_duplicateNames_throwDataLoadingException() {
        Path filePath = SERIALIZABLE_TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class,
                IllegalValueException.class.getName() + ": " + JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                jsonAddressBookStorage::readAddressBook);
    }

    @Test
    public void readAddressBook_missingIds_generatesDistinctNonNullIds() throws Exception {
        ReadOnlyAddressBook original = readAddressBook("legacyAddressBookWithoutIds.json").get();

        assertEquals(2, original.getPersonList().size());
        for (Person person : original.getPersonList()) {
            assertNotNull(person.getPersonId());
        }
        assertNotEquals(original.getPersonList().get(0).getPersonId(), original.getPersonList().get(1).getPersonId());
    }

    @Test
    public void readAddressBook_legacyIds_withoutSaveLeavesFileUnchanged() throws Exception {
        Path filePath = testFolder.resolve("UnsavedLegacyAddressBook.json");
        Files.copy(TEST_DATA_FOLDER.resolve("legacyPersonIdsAddressBook.json"), filePath);
        String json = Files.readString(filePath);
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);
        ReadOnlyAddressBook original = jsonAddressBookStorage.readAddressBook().get();

        assertEquals(2, original.getPersonList().size());
        for (Person person : original.getPersonList()) {
            assertNotNull(person.getPersonId());
        }
        assertEquals(json, Files.readString(filePath));

        ReadOnlyAddressBook readBack = new JsonAddressBookStorage(filePath).readAddressBook().get();
        assertEquals(original, new AddressBook(readBack));
        for (int i = 0; i < original.getPersonList().size(); i++) {
            assertNotEquals(original.getPersonList().get(i).getPersonId(),
                    readBack.getPersonList().get(i).getPersonId());
        }
        assertEquals(json, Files.readString(filePath));
    }

    @Test
    public void readAndSaveAddressBook_validStoredIds_preservesEveryId() throws Exception {
        ReadOnlyAddressBook original = new JsonAddressBookStorage(
                SERIALIZABLE_TEST_DATA_FOLDER.resolve("validPersonIdsAddressBook.json")).readAddressBook().get();
        List<PersonId> personIds = List.of(
                PersonId.fromString("550e8400-e29b-41d4-a716-446655440001"),
                PersonId.fromString("550e8400-e29b-41d4-a716-446655440002"));
        assertEquals(personIds, original.getPersonList().stream().map(Person::getPersonId).toList());

        Path filePath = testFolder.resolve("AddressBookWithIds.json");
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);
        jsonAddressBookStorage.saveAddressBook(original);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook().get();

        assertEquals(original, new AddressBook(readBack));
        assertEquals(personIds, readBack.getPersonList().stream().map(Person::getPersonId).toList());
    }

    @Test
    public void saveAddressBook_validPersons_writesEveryIdProperty() throws Exception {
        AddressBook original = getTypicalAddressBook();
        Path filePath = testFolder.resolve("SavedAddressBookWithIds.json");
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);
        jsonAddressBookStorage.saveAddressBook(original);

        JsonNode json = JsonUtil.readJsonFile(filePath, JsonNode.class).get();
        JsonNode persons = json.get("persons");
        assertNotNull(persons);
        assertTrue(persons.isArray());
        assertEquals(original.getPersonList().size(), persons.size());
        for (int i = 0; i < persons.size(); i++) {
            JsonNode person = persons.get(i);
            assertTrue(person.hasNonNull("id"));
            assertTrue(person.get("id").isTextual());
            assertEquals(original.getPersonList().get(i).getPersonId().toString(), person.get("id").textValue());
        }
    }

    @Test
    public void readAndSaveAddressBook_legacyIds_preservesGeneratedIds() throws Exception {
        ReadOnlyAddressBook original = readAddressBook("legacyPersonIdsAddressBook.json").get();
        assertEquals(2, original.getPersonList().size());
        for (Person person : original.getPersonList()) {
            assertNotNull(person.getPersonId());
        }
        assertNotEquals(original.getPersonList().get(0).getPersonId(), original.getPersonList().get(1).getPersonId());

        Path filePath = testFolder.resolve("LegacyAddressBook.json");
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);
        jsonAddressBookStorage.saveAddressBook(original);
        ReadOnlyAddressBook readBack = new JsonAddressBookStorage(filePath).readAddressBook().get();

        assertEquals(original, new AddressBook(readBack));
        assertEquals(original.getPersonList().stream().map(Person::getPersonId).toList(),
                readBack.getPersonList().stream().map(Person::getPersonId).toList());
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));
        assertEquals(original.getPersonList().stream().map(Person::getPersonId).toList(),
                readBack.getPersonList().stream().map(Person::getPersonId).toList());

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));
        assertEquals(original.getPersonList().stream().map(Person::getPersonId).toList(),
                readBack.getPersonList().stream().map(Person::getPersonId).toList());

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));
        assertEquals(original.getPersonList().stream().map(Person::getPersonId).toList(),
                readBack.getPersonList().stream().map(Person::getPersonId).toList());

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
