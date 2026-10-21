package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SetRoleCommand;
import seedu.address.model.membership.Role;

/** Tests parsing of {@code setrole} arguments. */
public class SetRoleCommandParserTest {

    private final SetRoleCommandParser parser = new SetRoleCommandParser();
    private static final String USAGE = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SetRoleCommand.MESSAGE_USAGE);

    @Test
    public void parse_validArguments_returnsCommand() {
        assertParseSuccess(parser, " c/2 pr/1 r/Implement the parser",
                new SetRoleCommand(Index.fromOneBased(2), Index.fromOneBased(1), new Role("Implement the parser")));
    }

    @Test
    public void parse_reversedPrefixOrder_returnsCommand() {
        assertParseSuccess(parser, " r/Review PRs pr/1 c/2",
                new SetRoleCommand(Index.fromOneBased(2), Index.fromOneBased(1), new Role("Review PRs")));
    }

    @Test
    public void parse_missingOrInvalidArgument_fails() {
        assertParseFailure(parser, " c/2 pr/1", USAGE);
        assertParseFailure(parser, " c/2 pr/1 r/", USAGE);
        assertParseFailure(parser, " c/0 pr/1 r/Build feature", USAGE + "\nIndex must be a positive integer.");
        assertParseFailure(parser, " c/2 pr/1 r/First r/Second",
                seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ROLE));
    }
}
