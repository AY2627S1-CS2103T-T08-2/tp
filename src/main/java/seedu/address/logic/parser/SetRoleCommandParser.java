package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import seedu.address.logic.commands.SetRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@code setrole}. */
public class SetRoleCommandParser implements Parser<SetRoleCommand> {

    @Override
    public SetRoleCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_CONTACT, PREFIX_PROJECT, PREFIX_ROLE);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_CONTACT, PREFIX_PROJECT, PREFIX_ROLE);
        String usageMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SetRoleCommand.MESSAGE_USAGE);
        if (argMultimap.getValue(PREFIX_CONTACT).isEmpty() || argMultimap.getValue(PREFIX_PROJECT).isEmpty()
                || argMultimap.getValue(PREFIX_ROLE).isEmpty() || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(usageMessage);
        }
        try {
            return new SetRoleCommand(
                    ParserUtil.parseIndex(argMultimap.getValue(PREFIX_CONTACT).get()),
                    ParserUtil.parseIndex(argMultimap.getValue(PREFIX_PROJECT).get()),
                    ParserUtil.parseRole(argMultimap.getValue(PREFIX_ROLE).get()));
        } catch (ParseException exception) {
            throw new ParseException(usageMessage + "\n" + exception.getMessage(), exception);
        }
    }
}
