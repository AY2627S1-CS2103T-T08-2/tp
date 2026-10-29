package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ListProjectsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for listing all projects or a displayed contact's projects.
 */
public class ListProjectsCommandParser implements Parser<ListProjectsCommand> {

    /**
     * Parses an optional contact index and creates a ListProjectsCommand.
     *
     * @throws ParseException if the arguments have invalid syntax.
     */
    @Override
    public ListProjectsCommand parse(String args) throws ParseException {
        if (args.isBlank()) {
            return new ListProjectsCommand();
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_CONTACT);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_CONTACT);

        String usageMessage = String.format(
            MESSAGE_INVALID_COMMAND_FORMAT, ListProjectsCommand.MESSAGE_USAGE);
        if (argMultimap
            .getValue(PREFIX_CONTACT)
            .isEmpty() || !argMultimap
            .getPreamble()
            .isEmpty()) {
            throw new ParseException(usageMessage);
        }

        try {
            Index contactIndex = ParserUtil.parseIndex(argMultimap
                .getValue(PREFIX_CONTACT)
                .get());
            return new ListProjectsCommand(contactIndex);
        } catch (ParseException pe) {
            throw new ParseException(usageMessage, pe);
        }
    }
}
