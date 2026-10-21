package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;

public class StorageManagerTest {

    @TempDir
    public Path testFolder;

    private StorageManager storageManager;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(getTempFilePath("ab"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        storageManager = new StorageManager(addressBookStorage, userPrefsStorage);
    }

    private Path getTempFilePath(String fileName) {
        return testFolder.resolve(fileName);
    }

    @Test
    public void prefsReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonUserPrefsStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link JsonUserPrefsStorageTest} class.
         */
        UserPrefs original = new UserPrefs();
        original.setGuiSettings(new GuiSettings(300, 600, 4, 6));
        storageManager.saveUserPrefs(original);
        UserPrefs retrieved = storageManager.readUserPrefs().get();
        assertEquals(original, retrieved);
    }

    @Test
    public void addressBookReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonAddressBookStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link JsonAddressBookStorageTest} class.
         */
        AddressBook original = getTypicalAddressBook();
        storageManager.saveAddressBook(original);
        ReadOnlyAddressBook retrieved = storageManager.readAddressBook().get();
        assertEquals(original, new AddressBook(retrieved));
        assertFalse(storageManager.isAddressBookSavingBlocked());
        storageManager.saveAddressBook(original);
    }

    @Test
    public void readAddressBook_missingFile_allowsSaving() throws Exception {
        assertTrue(storageManager.readAddressBook().isEmpty());
        assertFalse(storageManager.isAddressBookSavingBlocked());

        storageManager.saveAddressBook(getTypicalAddressBook());
        assertEquals(getTypicalAddressBook(), storageManager.readAddressBook().get());
    }

    @Test
    public void readAddressBook_loadingFailure_blocksSavingAndPreservesFile() throws Exception {
        Path filePath = storageManager.getAddressBookFilePath();
        String invalidJson = "{ invalid JSON";
        Files.writeString(filePath, invalidJson);

        assertThrows(DataLoadingException.class, () -> storageManager.readAddressBook());
        assertTrue(storageManager.isAddressBookSavingBlocked());
        assertThrows(IOException.class, () -> storageManager.saveAddressBook(new AddressBook()));
        assertEquals(invalidJson, Files.readString(filePath));

        UserPrefs userPrefs = new UserPrefs();
        storageManager.saveUserPrefs(userPrefs);
        assertEquals(userPrefs, storageManager.readUserPrefs().get());
    }

    @Test
    public void readAddressBook_successfulRetry_savingRemainsBlocked() throws Exception {
        Path filePath = storageManager.getAddressBookFilePath();
        Files.writeString(filePath, "{ invalid JSON");
        assertThrows(DataLoadingException.class, () -> storageManager.readAddressBook());

        new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook());
        String repairedJson = Files.readString(filePath);
        assertEquals(getTypicalAddressBook(), storageManager.readAddressBook().get());
        assertTrue(storageManager.isAddressBookSavingBlocked());
        assertThrows(IOException.class, () -> storageManager.saveAddressBook(new AddressBook()));
        assertEquals(repairedJson, Files.readString(filePath));
    }

    @Test
    public void getAddressBookFilePath() {
        assertNotNull(storageManager.getAddressBookFilePath());
    }

}
