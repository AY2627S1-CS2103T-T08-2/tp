package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        CommandResult commandResult;
        AddressBook beforeCommand = new AddressBook(model.getAddressBook());
        Predicate<? super Person> previousFilter = model.getFilteredPersonListPredicate();
        Command command = addressBookParser.parseCommand(commandText);
        commandResult = command.execute(model);

        if (storage.isAddressBookSavingBlocked()) {
            return new CommandResult(
                    commandResult.getFeedbackToUser()
                            + "\nSaving is disabled because the data file could not be loaded."
                            + " Changes in this session will be lost.",
                    commandResult.isShowHelp(),
                    commandResult.isExit());
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException e) {
            String errorMessage = String.format(
                    e instanceof AccessDeniedException ? FILE_OPS_PERMISSION_ERROR_FORMAT : FILE_OPS_ERROR_FORMAT,
                    e.getMessage());
            logger.warning(errorMessage);

            if (beforeCommand.equals(model.getAddressBook())) {
                return new CommandResult(commandResult.getFeedbackToUser() + "\nWarning: " + errorMessage,
                        commandResult.isShowHelp(), commandResult.isExit());
            }

            model.setAddressBook(beforeCommand);
            model.updateFilteredPersonList(previousFilter == null
                    ? Model.PREDICATE_SHOW_ALL_PERSONS : previousFilter::test);
            throw new CommandException(errorMessage
                    + "\nThe command was not applied. Fix the save problem and try again.", e);
        }

        return commandResult;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public ObservableList<Project> getProjectList() {
        return model.getProjectList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
