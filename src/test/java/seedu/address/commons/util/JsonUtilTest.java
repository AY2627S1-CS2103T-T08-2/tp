package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @TempDir
    public Path testFolder;

    @Test
    public void readJsonFile_missingFile_returnsEmpty() throws Exception {
        assertTrue(JsonUtil.readJsonFile(testFolder.resolve("missing.json"), SerializableTestClass.class).isEmpty());
    }

    @Test
    public void readJsonFile_validFile_returnsObject() throws Exception {
        Path filePath = testFolder.resolve("valid.json");
        Files.writeString(filePath, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass result = JsonUtil.readJsonFile(filePath, SerializableTestClass.class).orElseThrow();
        assertEquals(SerializableTestClass.getNameTestValue(), result.getName());
        assertEquals(SerializableTestClass.getListTestValues(), result.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), result.getMapOfIntegerToString());
    }

    @Test
    public void readJsonFile_malformedFile_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("invalid.json");
        Files.writeString(filePath, "{ invalid JSON");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(filePath, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_directory_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(testFolder, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_inaccessibleParent_throwsDataLoadingException() throws Exception {
        assumeTrue(Files.getFileStore(testFolder).supportsFileAttributeView("posix"),
                "This test requires POSIX file permissions.");
        Path parent = Files.createDirectory(testFolder.resolve("restricted"));
        Path filePath = parent.resolve("data.json");
        Files.writeString(filePath, SerializableTestClass.JSON_STRING_REPRESENTATION);
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(parent);
        try {
            Files.setPosixFilePermissions(parent, Set.of());
            assumeTrue(!Files.isReadable(filePath), "The environment must enforce the read restriction.");
            assertFalse(Files.exists(filePath));
            assertFalse(Files.notExists(filePath));
            assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(
                    filePath, SerializableTestClass.class));
        } finally {
            Files.setPosixFilePermissions(parent, originalPermissions);
        }
    }

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
