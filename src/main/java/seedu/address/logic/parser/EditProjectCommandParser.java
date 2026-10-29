package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.Description;

/**
 * Parses input arguments and creates a new EditProjectCommand object
 */
public class EditProjectCommandParser implements Parser<EditProjectCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EditProjectCommand
     * and returns an EditProjectCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);

        if (args
            .codePoints()
            .anyMatch(codePoint -> codePoint == '\r'
                || codePoint == '\n'
                || codePoint == '\u0085'
                || codePoint == '\u2028'
                || codePoint == '\u2029')) {
            throw new ParseException(String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, EditProjectCommand.MESSAGE_USAGE));
        }

        ArgumentMultimap argMultimap =
            ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_DEADLINE, PREFIX_DESCRIPTION);

        Index index;

        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditProjectCommand.MESSAGE_USAGE),
                pe
            );
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_DEADLINE, PREFIX_DESCRIPTION);

        EditProjectCommand.EditProjectDescriptor editProjectDescriptor = new EditProjectCommand.EditProjectDescriptor();

        if (argMultimap
            .getValue(PREFIX_NAME)
            .isPresent()) {
            editProjectDescriptor.setName(ParserUtil.parseProjectName(argMultimap
                .getValue(PREFIX_NAME)
                .get()));
        }
        if (argMultimap
            .getValue(PREFIX_DEADLINE)
            .isPresent()) {
            editProjectDescriptor.setDeadline(ParserUtil.parseDeadline(argMultimap
                .getValue(PREFIX_DEADLINE)
                .get()));
        }
        if (argMultimap
            .getValue(PREFIX_DESCRIPTION)
            .isPresent()) {
            String description = argMultimap
                .getValue(PREFIX_DESCRIPTION)
                .get();
            editProjectDescriptor.setDescription(description.isBlank()
                ? Description.EMPTY
                : ParserUtil.parseDescription(description));
        }

        if (!editProjectDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditProjectCommand.MESSAGE_NOT_EDITED);
        }

        return new EditProjectCommand(index, editProjectDescriptor);
    }
}
