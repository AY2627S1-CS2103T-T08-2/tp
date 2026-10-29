package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ListProjectsCommand;

public class ListProjectsCommandParserTest {

    private static final String USAGE_MESSAGE = String.format(
        MESSAGE_INVALID_COMMAND_FORMAT, ListProjectsCommand.MESSAGE_USAGE);

    private final ListProjectsCommandParser parser = new ListProjectsCommandParser();

    @Test
    public void parse_noArguments_returnsAllProjectsCommand() {
        for (String args : new String[] {"", " ", " \t "}) {
            assertParseSuccess(parser, args, new ListProjectsCommand());
        }
    }

    @Test
    public void parse_contactIndex_returnsContactProjectsCommand() {
        assertParseSuccess(parser, " c/2", new ListProjectsCommand(Index.fromOneBased(2)));
        assertParseSuccess(parser, "   c/  2   ", new ListProjectsCommand(Index.fromOneBased(2)));
        assertParseSuccess(parser, " c/2147483647",
            new ListProjectsCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_emptyContactIndex_failure() {
        for (String args : new String[] {" c/", " c/   "}) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_invalidContactIndex_failure() {
        String[] invalidArgs = {" c/0", " c/-1", " c/+1", " c/1.5", " c/abc", " c/2147483648", " c/1 2"};
        for (String args : invalidArgs) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_duplicateContactPrefix_failure() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTACT);
        for (String args : new String[] {" c/1 c/2", " c/1 c/1", " c/ c/2"}) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_missingOrMalformedPrefix_failure() {
        String[] invalidArgs = {" 2", " c2", " c /2", " contact/2", " C/2", " unexpected c/2"};
        for (String args : invalidArgs) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_unsupportedArguments_failure() {
        String[] invalidArgs = {" pr/1", " x/3 c/2", " c/2 x/3", " c/2pr/1", " c/2 unexpected"};
        for (String args : invalidArgs) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }
}
