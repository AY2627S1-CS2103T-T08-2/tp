package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AssociateCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DissociateCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.Project;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ProjectBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_clearWithExtraArguments_preservesDataAndSavedFile() throws Exception {
        Project project = new ProjectBuilder().build();
        model.addPerson(AMY);
        model.addProject(project);
        model.addProjectMembership(new ProjectMembership(AMY.getPersonId(), project.getProjectId()));
        logic.execute(ListCommand.COMMAND_WORD);
        Path addressBookPath = temporaryFolder.resolve("addressBook.json");
        String savedJson = Files.readString(addressBookPath);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> logic.execute("clear 3"));

        assertEquals(expectedModel, model);
        assertEquals(savedJson, Files.readString(addressBookPath));
        assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
        assertEquals(List.of(project), model.getAddressBook().getProjectList());
        assertEquals(List.of(new ProjectMembership(AMY.getPersonId(), project.getProjectId())),
                model.getAddressBook().getProjectMembershipList());
    }

    @Test
    public void execute_associateCommand_success() throws Exception {
        Project project = new ProjectBuilder().withName("CS2103T Team Project").build();
        model.addPerson(AMY);
        model.addProject(project);
        ProjectMembership membership = new ProjectMembership(AMY.getPersonId(), project.getProjectId());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addProjectMembership(membership);

        assertCommandSuccess("associate c/1 pr/1",
                String.format(AssociateCommand.MESSAGE_SUCCESS, AMY.getName(), project.getName()), expectedModel);
        assertEquals(List.of(membership), model.getAddressBook().getProjectMembershipList());
    }

    @Test
    public void execute_associateCommandProjectListOrder_success() throws Exception {
        List<Project> displayedProjects = logic.getProjectList();
        Project firstProject = new ProjectBuilder().withName("Zeta").build();
        Project secondProject = new ProjectBuilder().withName("Alpha").build();
        model.addPerson(AMY);
        model.addProject(firstProject);
        model.addProject(secondProject);

        assertEquals(List.of(firstProject, secondProject), displayedProjects);
        assertEquals(secondProject.getProjectId(), displayedProjects.get(1).getProjectId());
        ProjectMembership membership = new ProjectMembership(AMY.getPersonId(), secondProject.getProjectId());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addProjectMembership(membership);

        assertCommandSuccess("associate c/1 pr/2",
                String.format(AssociateCommand.MESSAGE_SUCCESS, AMY.getName(), secondProject.getName()), expectedModel);
        assertEquals(List.of(membership), model.getAddressBook().getProjectMembershipList());
    }

    @Test
    public void execute_associateCommandInvalidSyntax_throwsParseException() {
        model.addPerson(AMY);
        model.addProject(new ProjectBuilder().build());
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AssociateCommand.MESSAGE_USAGE);

        assertParseException("associate c/1", expectedMessage);
        assertParseException("associate c/1 pr/0", expectedMessage);
        assertParseException("associate c/1 pr/1 x/2", expectedMessage);
    }

    @Test
    public void execute_associateCommandInvalidIndices_throwsCommandException() {
        model.addPerson(AMY);
        model.addProject(new ProjectBuilder().build());

        assertCommandException("associate c/2 pr/1", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandException("associate c/1 pr/2", MESSAGE_INVALID_PROJECT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_associateCommandDuplicate_throwsCommandException() throws Exception {
        Project project = new ProjectBuilder().withName("CS2103T Team Project").build();
        model.addPerson(AMY);
        model.addProject(project);
        logic.execute("associate c/1 pr/1");
        Path addressBookPath = temporaryFolder.resolve("addressBook.json");
        String savedJson = Files.readString(addressBookPath);

        assertCommandException("associate c/1 pr/1",
                String.format(AssociateCommand.MESSAGE_DUPLICATE_ASSOCIATION, AMY.getName(), project.getName()));
        assertEquals(savedJson, Files.readString(addressBookPath));
    }

    @Test
    public void execute_dissociateCommand_persistsRemovalAfterReload() throws Exception {
        Project firstProject = new ProjectBuilder().withName("CS2103T Team Project").build();
        Project secondProject = new ProjectBuilder().withName("Orbital").build();
        model.addPerson(AMY);
        model.addPerson(BENSON);
        model.addProject(firstProject);
        model.addProject(secondProject);
        logic.execute("associate c/1 pr/1");
        logic.execute("associate c/1 pr/2");
        logic.execute("associate c/2 pr/1");
        ProjectMembership selectedMembership = new ProjectMembership(AMY.getPersonId(), firstProject.getProjectId());
        ProjectMembership sameContactMembership =
                new ProjectMembership(AMY.getPersonId(), secondProject.getProjectId());
        ProjectMembership sameProjectMembership =
                new ProjectMembership(BENSON.getPersonId(), firstProject.getProjectId());
        Path filePath = temporaryFolder.resolve("addressBook.json");
        ReadOnlyAddressBook beforeRemoval = new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow();
        assertEquals(List.of(selectedMembership, sameContactMembership, sameProjectMembership),
                beforeRemoval.getProjectMembershipList());

        CommandResult result = logic.execute("dissociate c/1 pr/1");

        ReadOnlyAddressBook restored = new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow();
        Model restartedModel = new ModelManager(restored, new UserPrefs());
        assertEquals(String.format(DissociateCommand.MESSAGE_SUCCESS, AMY.getName(), firstProject.getName()),
                result.getFeedbackToUser());
        assertFalse(restartedModel.hasProjectMembership(selectedMembership));
        assertEquals(List.of(sameContactMembership, sameProjectMembership),
                restartedModel.getAddressBook().getProjectMembershipList());
        assertEquals(List.of(AMY, BENSON), restartedModel.getAddressBook().getPersonList());
        assertEquals(List.of(AMY.getPersonId(), BENSON.getPersonId()),
                restartedModel.getAddressBook().getPersonList().stream().map(Person::getPersonId).toList());
        assertEquals(List.of(firstProject, secondProject), restartedModel.getProjectList());
        assertEquals(List.of(firstProject.getProjectId(), secondProject.getProjectId()),
                restartedModel.getProjectList().stream().map(Project::getProjectId).toList());
        assertEquals(List.of(BENSON), restartedModel.getProjectMembers(firstProject));
        assertEquals(List.of(AMY), restartedModel.getProjectMembers(secondProject));
        assertEquals(model, restartedModel);
    }

    @Test
    public void execute_listCommand_persistsGeneratedLegacyIds() throws Exception {
        Path filePath = temporaryFolder.resolve("LegacyAddressBook.json");
        Files.copy(Paths.get("src", "test", "data", "JsonAddressBookStorageTest", "legacyPersonIdsAddressBook.json"),
                filePath);
        String json = Files.readString(filePath);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(filePath);
        ReadOnlyAddressBook original = addressBookStorage.readAddressBook().get();
        List<PersonId> personIds = original.getPersonList().stream().map(Person::getPersonId).toList();
        model = new ModelManager(original, new UserPrefs());
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("LegacyUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);

        assertEquals(json, Files.readString(filePath));
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);

        ReadOnlyAddressBook readBack = new JsonAddressBookStorage(filePath).readAddressBook().get();
        assertEquals(personIds, readBack.getPersonList().stream().map(Person::getPersonId).toList());
        assertEquals(original, readBack);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_loadingFailure_preservesFileAndCommandResultFlags() throws Exception {
        Path filePath = temporaryFolder.resolve("invalidAddressBook.json");
        String invalidJson = "{ invalid JSON";
        Files.writeString(filePath, invalidJson);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(filePath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
        logic = new LogicManager(model, storage);
        Model expectedModel = new ModelManager();
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;

        for (String commandText : List.of("list", "listprojects", "help", addCommand, "exit")) {
            CommandResult expectedResult = new AddressBookParser()
                    .parseCommand(commandText).execute(expectedModel);
            CommandResult actualResult = logic.execute(commandText);
            assertEquals(new CommandResult(expectedResult.getFeedbackToUser()
                    + "\nSaving is disabled because the data file could not be loaded."
                    + " Changes in this session will be lost.",
                    expectedResult.isShowHelp(), expectedResult.isExit()), actualResult);
            assertEquals(expectedModel, model);
            assertEquals(invalidJson, Files.readString(filePath));
        }
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void getProjectList_addProject_updatesList() {
        Project project = new ProjectBuilder().build();
        model.addProject(project);

        assertEquals(List.of(project), logic.getProjectList());
    }

    @Test
    public void getProjectList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getProjectList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Model expectedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        assertCommandFailure(
                addCommand,
                CommandException.class,
                expectedMessage + "\nThe command was not applied. Fix the save problem and try again.",
                expectedModel
        );
    }
}
