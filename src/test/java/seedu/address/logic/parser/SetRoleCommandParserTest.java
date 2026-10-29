package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SetRoleCommand;
import seedu.address.model.membership.Role;

/**
 * Tests parsing of {@code setrole} arguments.
 */
public class SetRoleCommandParserTest {

    private static final String USAGE = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SetRoleCommand.MESSAGE_USAGE);
    private final SetRoleCommandParser parser = new SetRoleCommandParser();

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
        assertParseFailure(parser, " pr/1 r/Implement the parser", USAGE);
        assertParseFailure(parser, " c/2 r/Implement the parser", USAGE);
        assertParseFailure(parser, " c/2 pr/1", USAGE);
        assertParseFailure(parser, " c/2 pr/1 r/" + "x".repeat(201),
            USAGE + "\n" + Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/2 pr/1 r/line\nbreak", USAGE + "\n" + Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " extra c/2 pr/1 r/Implement the parser", USAGE);
        assertParseFailure(parser, " c/0 pr/1 r/Build feature", USAGE + "\nIndex must be a positive integer.");
        assertParseFailure(parser, " c/2 pr/1 r/First r/Second",
            seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ROLE));
    }

    @Test
    public void parse_emptyOrWhitespaceRole_returnsClearCommand() {
        SetRoleCommand expected = new SetRoleCommand(
            Index.fromOneBased(2), Index.fromOneBased(1), Role.EMPTY);

        assertParseSuccess(parser, " c/2 pr/1 r/", expected);
        assertParseSuccess(parser, " c/2 pr/1 r/   \t ", expected);
        assertParseSuccess(parser, " r/ pr/1 c/2", expected);
        assertParseSuccess(parser, " pr/1 r/ c/2", expected);
    }

    @Test
    public void parse_duplicateEmptyRole_failure() {
        assertParseFailure(parser, " c/2 pr/1 r/ r/",
            seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ROLE));
    }

    @Test
    public void parse_emptyRoleWithInvalidIndex_failure() {
        assertParseFailure(parser, " c/0 pr/1 r/", USAGE + "\nIndex must be a positive integer.");
        assertParseFailure(parser, " c/2 pr/0 r/", USAGE + "\nIndex must be a positive integer.");
    }
}
