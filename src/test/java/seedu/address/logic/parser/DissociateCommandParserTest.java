package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.DissociateCommand;

/**
 * Tests parsing of contact and project indices for dissociation.
 */
public class DissociateCommandParserTest {

    private static final String USAGE_MESSAGE = String.format(
            MESSAGE_INVALID_COMMAND_FORMAT, DissociateCommand.MESSAGE_USAGE);

    private final DissociateCommandParser parser = new DissociateCommandParser();

    @Test
    public void parse_validArgs_returnsDissociateCommand() {
        assertParseSuccess(parser, " c/2 pr/1",
                new DissociateCommand(Index.fromOneBased(2), Index.fromOneBased(1)));
    }

    @Test
    public void parse_reversedPrefixOrder_success() {
        assertParseSuccess(parser, " pr/1 c/2",
                new DissociateCommand(Index.fromOneBased(2), Index.fromOneBased(1)));
    }

    @Test
    public void parse_extraWhitespace_success() {
        assertParseSuccess(parser, "   c/  2   pr/  1   ",
                new DissociateCommand(Index.fromOneBased(2), Index.fromOneBased(1)));
    }

    @Test
    public void parse_largeValidIndices_success() {
        assertParseSuccess(parser, " c/2147483647 pr/2147483647",
                new DissociateCommand(Index.fromOneBased(Integer.MAX_VALUE), Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_missingPrefixes_failure() {
        String[] invalidArgs = {"", " ", " c/2", " pr/1", " 2 1"};

        for (String args : invalidArgs) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_emptyValues_failure() {
        String[] invalidArgs = {" c/ pr/1", " c/2 pr/", " c/ pr/ ", " c/   pr/1"};

        for (String args : invalidArgs) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, " unexpected c/2 pr/1", USAGE_MESSAGE);
    }

    @Test
    public void parse_malformedPrefixes_failure() {
        String[] invalidArgs = {
            " c2 pr/1", " c/2 pr1", " c /2 pr/1", " c/2 proj/1", " c/2pr/1", " c/2 pr/1/pr/2"
        };

        for (String args : invalidArgs) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_unsupportedArguments_failure() {
        String[] invalidArgs = {
            " x/3 c/2 pr/1", " c/2 x/3 pr/1", " c/2 pr/1 x/3", " c/2 pr/1 unexpected"
        };

        for (String args : invalidArgs) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_duplicateContactPrefix_failure() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTACT);
        String[] invalidArgs = {" c/2 c/3 pr/1", " c/2 c/2 pr/1", " c/ c/2 pr/1", " c/2 c/3"};

        for (String args : invalidArgs) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_duplicateProjectPrefix_failure() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PROJECT);
        String[] invalidArgs = {" c/2 pr/1 pr/2", " c/2 pr/1 pr/1", " c/2 pr/ pr/1", " pr/1 pr/2"};

        for (String args : invalidArgs) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_bothPrefixesRepeated_failure() {
        assertParseFailure(parser, " c/1 c/2 pr/1 pr/2",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTACT, PREFIX_PROJECT));
    }

    @Test
    public void parse_invalidIndices_failure() {
        String[] invalidIndices = {"0", "-1", "+1", "abc", "1.5", "2147483648", "1 2"};

        for (String index : invalidIndices) {
            assertParseFailure(parser, " c/" + index + " pr/1", USAGE_MESSAGE);
            assertParseFailure(parser, " c/2 pr/" + index, USAGE_MESSAGE);
        }
    }
}
