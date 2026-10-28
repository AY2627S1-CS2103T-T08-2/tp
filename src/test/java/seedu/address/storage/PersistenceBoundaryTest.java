package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.ProjectBuilder;

/** Exercises real-file persistence, malformed data protection, and save failure boundaries. */
public class PersistenceBoundaryTest {
    private static final Person PERSON = new PersonBuilder().build();
    private static final Project PROJECT = new ProjectBuilder().build();

    @TempDir
    private Path directory;

    @TestFactory
    public Stream<DynamicTest> read_invalidDocumentPartitions_blocksSavingAndPreservesBytes() {
        return Stream.of("", " ", "{", "[]", "42", "true", "\"text\"", "null",
                "{\"persons\":[null]}", "{\"projects\":[null]}", "{\"memberships\":[null]}",
                "{\"persons\":[{}]}", "{\"projects\":[{}]}", "{\"memberships\":[{}]}",
                "{} trailing", "{} {}", "{} []", "{} null")
                .map(json -> dynamicTest("invalid document " + json, () -> assertProtected(json)));
    }

    @Test
    public void read_nullTagElement_blocksSavingAndPreservesBytes() throws Exception {
        String person = JsonUtil.toJsonString(new JsonAdaptedPerson(PERSON));
        var node = JsonUtil.fromJsonString(person, com.fasterxml.jackson.databind.node.ObjectNode.class);
        node.putArray("tags").addNull();
        assertProtected("{\"persons\":[" + node + "]}");
    }

    @TestFactory
    public Stream<DynamicTest> read_invalidEntityAndReferencePartitions_blocksSaving() {
        return IntStream.range(0, 10).mapToObj(partition -> dynamicTest("invalid model partition " + partition, () -> {
            AddressBook book = new AddressBook();
            book.addPerson(PERSON);
            book.addProject(PROJECT);
            book.addProjectMembership(new ProjectMembership(PERSON.getPersonId(), PROJECT.getProjectId()));
            var root = JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonSerializableAddressBook(book)),
                    com.fasterxml.jackson.databind.node.ObjectNode.class);
            var person = (com.fasterxml.jackson.databind.node.ObjectNode) root.get("persons").get(0);
            var project = (com.fasterxml.jackson.databind.node.ObjectNode) root.get("projects").get(0);
            var membership = (com.fasterxml.jackson.databind.node.ObjectNode) root.get("memberships").get(0);
            switch (partition) {
                case 0:
                    person.put("name", "");
                    break;
                case 1:
                    person.put("id", "invalid");
                    break;
                case 2:
                    project.put("id", "invalid");
                    break;
                case 3:
                    membership.remove("personId");
                    break;
                case 4:
                    membership.put("personId", "ffffffff-ffff-ffff-ffff-ffffffffffff");
                    break;
                case 5:
                    membership.put("projectId", "ffffffff-ffff-ffff-ffff-ffffffffffff");
                    break;
                case 6:
                    membership.put("role", "x".repeat(201));
                    break;
                case 7:
                    root.withArray("memberships").add(membership.deepCopy().put("role", "Other role"));
                    break;
                case 8:
                    root.withArray("persons").add(person.deepCopy().put("phone", "123"));
                    break;
                default:
                    root.withArray("projects").add(project.deepCopy().put("name", "Different"));
                    break;
            }
            assertProtected(root.toString());
        }));
    }

    @TestFactory
    public Stream<DynamicTest> roundTrip_allTwoByTwoGraphs_preservesIdsRolesAndOrder() {
        return IntStream.range(0, 16).mapToObj(mask -> dynamicTest("round trip graph " + mask, () -> {
            AddressBook book = new AddressBook();
            Person other = new PersonBuilder(PERSON).withPhone("123")
                    .withPersonId(seedu.address.model.person.PersonId.generate()).build();
            Project otherProject = new ProjectBuilder().withName("Other").build();
            List<Person> persons = List.of(PERSON, other);
            List<Project> projects = List.of(PROJECT, otherProject);
            persons.forEach(book::addPerson);
            projects.forEach(book::addProject);
            for (int index = 3; index >= 0; index--) {
                if ((mask & (1 << index)) != 0) {
                    book.addProjectMembership(new ProjectMembership(persons.get(index / 2).getPersonId(),
                            projects.get(index % 2).getProjectId(), new Role(index == 0 ? "" : "x".repeat(200))));
                }
            }
            Path path = directory.resolve("graph-" + mask + ".json");
            StorageManager storage = storage(path);
            assertTrue(storage.readAddressBook().isEmpty());
            for (int cycle = 0; cycle < 3; cycle++) {
                storage.saveAddressBook(book);
                ReadOnlyAddressBook restored = storage.readAddressBook().orElseThrow();
                assertEquals(book, restored);
                assertEquals(persons.stream().map(Person::getPersonId).toList(),
                        restored.getPersonList().stream().map(Person::getPersonId).toList());
                assertEquals(book.getProjectMembershipList().stream().map(ProjectMembership::getRole).toList(),
                        restored.getProjectMembershipList().stream().map(ProjectMembership::getRole).toList());
                assertFalse(storage.isAddressBookSavingBlocked());
                book = new AddressBook(restored);
            }
        }));
    }

    @TestFactory
    public Stream<DynamicTest> read_legacyMissingOrNullOptionalFields_generatesStableIdsAfterSave() {
        return Stream.of(false, true).flatMap(explicitNull -> Stream.of(false, true).map(nullCollections ->
                dynamicTest("legacy id null " + explicitNull + " collections null " + nullCollections, () -> {
                    var person = JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonAdaptedPerson(PERSON)),
                            com.fasterxml.jackson.databind.node.ObjectNode.class);
                    if (explicitNull) {
                        person.putNull("id");
                    } else {
                        person.remove("id");
                    }
                    String json = "{\"persons\":[" + person + "]"
                            + (nullCollections ? ",\"projects\":null,\"memberships\":null}" : "}");
                    Path path = Files.createTempFile(directory, "legacy", ".json");
                    Files.writeString(path, json);
                    StorageManager storage = storage(path);
                    var loaded = storage.readAddressBook().orElseThrow();
                    storage.saveAddressBook(loaded);
                    var restored = storage.readAddressBook().orElseThrow();
                    assertEquals(loaded.getPersonList().getFirst().getPersonId(),
                            restored.getPersonList().getFirst().getPersonId());
                    assertTrue(restored.getProjectMembershipList().isEmpty());
                    assertTrue(restored.getProjectList().isEmpty());
                })));
    }

    @TestFactory
    public Stream<DynamicTest> execute_protectedSession_neverOverwritesUnreadableFile() {
        return Stream.of("help", "exit", "list", "listprojects", "clear",
                "add n/New Contact p/123 e/new@example.com a/Home")
                .map(command -> dynamicTest("protected command " + command, () -> {
                    Path path = Files.createTempFile(directory, "protected", ".json");
                    Files.writeString(path, "{ broken");
                    byte[] original = Files.readAllBytes(path);
                    StorageManager storage = storage(path);
                    assertThrows(DataLoadingException.class, storage::readAddressBook);
                    var result = new LogicManager(new ModelManager(), storage).execute(command);
                    assertTrue(result.getFeedbackToUser().contains("Saving is disabled"));
                    assertEquals(command.equals("help"), result.isShowHelp());
                    assertEquals(command.equals("exit"), result.isExit());
                    assertArrayEquals(original, Files.readAllBytes(path));
                }));
    }

    @TestFactory
    public Stream<DynamicTest> execute_saveFailure_rollsBackChangedMembershipData() {
        return Stream.of("associate c/1 pr/1", "dissociate c/1 pr/1", "setrole c/1 pr/1 r/Changed")
                .map(command -> dynamicTest("save failure " + command, () -> {
                    AddressBook book = new AddressBook();
                    book.addPerson(PERSON);
                    book.addProject(PROJECT);
                    if (!command.startsWith("associate")) {
                        book.addProjectMembership(new ProjectMembership(PERSON.getPersonId(), PROJECT.getProjectId(),
                                new Role("Original")));
                    }
                    StorageManager storage = new StorageManager(new JsonAddressBookStorage(directory.resolve("fail")) {
                        @Override
                        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                            throw new IOException("Simulated save failure");
                        }
                    }, new JsonUserPrefsStorage(directory.resolve("prefs.json")));
                    ModelManager model = new ModelManager(book, new UserPrefs());
                    LogicManager logic = new LogicManager(model, storage);
                    assertAll(() -> {
                        assertThrows(CommandException.class, () -> logic.execute(command));
                    }, () -> {
                        assertEquals(book, model.getAddressBook());
                    }, () -> {
                        assertEquals(book.getProjectMembershipList().stream().map(ProjectMembership::getRole).toList(),
                                model.getAddressBook().getProjectMembershipList().stream()
                                        .map(ProjectMembership::getRole).toList());
                    });
                }));
    }

    private void assertProtected(String json) throws Exception {
        Path path = Files.createTempFile(directory, "invalid", ".json");
        Files.writeString(path, json);
        byte[] original = Files.readAllBytes(path);
        StorageManager storage = storage(path);
        assertAll(() -> {
            assertThrows(DataLoadingException.class, storage::readAddressBook);
        }, () -> {
            assertTrue(storage.isAddressBookSavingBlocked(), "Failed load must block saving");
        }, () -> {
            assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
        }, () -> {
            assertArrayEquals(original, Files.readAllBytes(path));
        });
    }

    private StorageManager storage(Path path) {
        return new StorageManager(new JsonAddressBookStorage(path),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
    }
}
