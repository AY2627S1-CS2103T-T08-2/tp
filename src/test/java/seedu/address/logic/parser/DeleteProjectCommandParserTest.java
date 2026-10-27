package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tests positive-integer parsing and AddressBookParser routing for deleteproject.
 */
public class DeleteProjectCommandParserTest {

    private static final String USAGE_MESSAGE = String.format(
        MESSAGE_INVALID_COMMAND_FORMAT, DeleteProjectCommand.MESSAGE_USAGE);

    private final DeleteProjectCommandParser parser =
        new DeleteProjectCommandParser();
    private final AddressBookParser addressBookParser = new AddressBookParser();

    @Test
    public void parse_validIndices_returnsDeleteProjectCommand() {
        assertParseSuccess(
            parser, "1",
            new DeleteProjectCommand(Index.fromOneBased(1)));
        assertParseSuccess(
            parser, "2",
            new DeleteProjectCommand(Index.fromOneBased(2)));
        assertParseSuccess(
            parser, "2147483647",
            new DeleteProjectCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_surroundingWhitespace_success() {
        assertParseSuccess(
            parser, "  2  ",
            new DeleteProjectCommand(Index.fromOneBased(2)));
        assertParseSuccess(
            parser, "\t 2 \t",
            new DeleteProjectCommand(Index.fromOneBased(2)));
    }

    @Test
    public void parse_missingIndex_failure() {
        for (String args : new String[] {"", " ", " \t "}) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_zeroNegativeOrSignedIndex_failure() {
        for (String args : new String[] {"0", "-1", "+1", "-2147483648"}) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_nonIntegerIndex_failure() {
        for (String args : new String[] {"abc", "1.0", "1.5", "1e2", "0x1"}) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_integerOverflow_failure() {
        for (String args : new String[] {
            "2147483648", "999999999999999999999999"
        }) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_extraArgumentsOrMultipleIndices_failure() {
        for (String args : new String[] {
            "1 2", "1,2", "1 extra", "1 n/Project", "1 pr/2"
        }) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_prefixesInsteadOfPositionalIndex_failure() {
        for (String args : new String[] {
            "pr/1", "p/1", "c/1", "n/Zeta Project"
        }) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parseCommand_deleteProject_routesToProjectDeletion()
        throws Exception {
        assertEquals(
            new DeleteProjectCommand(Index.fromOneBased(2)),
            addressBookParser.parseCommand("deleteproject 2"));

        assertEquals(
            new DeleteProjectCommand(Index.fromOneBased(1)),
            addressBookParser.parseCommand(" \tdeleteproject \t1 \t"));
    }

    @Test
    public void parseCommand_invalidDeleteProjectArguments_showsProjectUsage() {
        for (String command : new String[] {
            "deleteproject", "deleteproject 0", "deleteproject 1 2"
        }) {
            assertThrows(
                ParseException.class,
                USAGE_MESSAGE,
                () -> addressBookParser.parseCommand(command));
        }
    }

    @Test
    public void parseCommand_largeValidIndex_defersBoundsCheckToCommand()
        throws Exception {
        assertEquals(
            new DeleteProjectCommand(Index.fromOneBased(Integer.MAX_VALUE)),
            addressBookParser.parseCommand("deleteproject 2147483647"));
    }
}
