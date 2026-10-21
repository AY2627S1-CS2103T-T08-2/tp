package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DissociateCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a DissociateCommand.
 */
public class DissociateCommandParser implements Parser<DissociateCommand> {

    /**
     * Parses contact and project indices for dissociation.
     *
     * @throws ParseException if the arguments have invalid syntax.
     */
    @Override
    public DissociateCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_CONTACT, PREFIX_PROJECT);

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_CONTACT, PREFIX_PROJECT);

        String usageMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, DissociateCommand.MESSAGE_USAGE);

        if (argMultimap.getValue(PREFIX_CONTACT).isEmpty()
                || argMultimap.getValue(PREFIX_PROJECT).isEmpty()
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(usageMessage);
        }

        try {
            Index contactIndex = ParserUtil.parseIndex(argMultimap.getValue(PREFIX_CONTACT).get());
            Index projectIndex = ParserUtil.parseIndex(argMultimap.getValue(PREFIX_PROJECT).get());

            return new DissociateCommand(contactIndex, projectIndex);
        } catch (ParseException pe) {
            throw new ParseException(usageMessage, pe);
        }
    }
}
