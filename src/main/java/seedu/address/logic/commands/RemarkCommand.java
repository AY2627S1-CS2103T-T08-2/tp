package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Changes the remark of an existing person in the address book.
 */
public class RemarkCommand extends Command {

    public static final String COMMAND_WORD = "remark";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Changes the remark of an existing person in the address book.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "REMARK (must be a string)\n"
            + "Example: " + COMMAND_WORD + " 1 This is a remark";
    public static final String MESSAGE_NOT_IMPLEMENTED_YET = "This command is not implemented yet.";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult("Hello from remark");
    }
}
