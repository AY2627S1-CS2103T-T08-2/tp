package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.logic.commands.FindProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.ProjectNameContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindProjectCommand object
 */
public class FindProjectCommandParser implements Parser<FindProjectCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindProjectCommand
     * and returns a FindProjectCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindProjectCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindProjectCommand.MESSAGE_USAGE));
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindProjectCommand(new ProjectNameContainsKeywordsPredicate(List.of(nameKeywords)));
    }
}
