package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;

import seedu.address.logic.commands.ListMembersCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.ProjectName;

/**
 * Parses arguments for the {@code listmembers} command.
 */
public class ListMembersCommandParser implements Parser<ListMembersCommand> {

    @Override
    public ListMembersCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_PROJECT);
        if (argMultimap
            .getValue(PREFIX_PROJECT)
            .isEmpty() || !argMultimap
            .getPreamble()
            .isEmpty()) {
            throw new ParseException(String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, ListMembersCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_PROJECT);
        ProjectName projectName = ParserUtil.parseProjectName(argMultimap
            .getValue(PREFIX_PROJECT)
            .get());
        return new ListMembersCommand(projectName);
    }
}
