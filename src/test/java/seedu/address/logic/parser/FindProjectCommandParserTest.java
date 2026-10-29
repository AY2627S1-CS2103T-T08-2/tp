package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.ProjectNameContainsKeywordsPredicate;

/**
 * Tests keyword parsing and AddressBookParser routing for findproject.
 */
public class FindProjectCommandParserTest {

    private static final String USAGE_MESSAGE = String.format(
        MESSAGE_INVALID_COMMAND_FORMAT, FindProjectCommand.MESSAGE_USAGE);

    private final FindProjectCommandParser parser = new FindProjectCommandParser();
    private final AddressBookParser addressBookParser = new AddressBookParser();

    @Test
    public void parse_emptyOrWhitespaceOnly_failure() {
        for (String args : new String[] {"", " ", " \t \n "}) {
            assertParseFailure(parser, args, USAGE_MESSAGE);
        }
    }

    @Test
    public void parse_singleKeyword_success() {
        assertParseSuccess(parser, "CS2103T", new FindProjectCommand(
            new ProjectNameContainsKeywordsPredicate(List.of("CS2103T"))));
    }

    @Test
    public void parse_multipleKeywordsAndWhitespace_success() {
        FindProjectCommand expected = new FindProjectCommand(
            new ProjectNameContainsKeywordsPredicate(List.of("Alpha", "bETA", "Gamma")));

        assertParseSuccess(parser, "Alpha bETA Gamma", expected);
        assertParseSuccess(parser, " \t Alpha   bETA \t Gamma \n ", expected);
    }

    @Test
    public void parseCommand_findProject_routesToProjectSearch() throws ParseException {
        FindProjectCommand expected = new FindProjectCommand(
            new ProjectNameContainsKeywordsPredicate(List.of("Alpha", "Beta")));

        assertEquals(expected, addressBookParser.parseCommand("findproject Alpha Beta"));
        assertEquals(expected, addressBookParser.parseCommand(" \tfindproject \t Alpha  Beta \t"));
    }

    @Test
    public void parseCommand_missingKeywords_showsProjectSearchUsage() {
        assertThrows(ParseException.class, USAGE_MESSAGE, () -> {
            addressBookParser.parseCommand("findproject");
        });
        assertThrows(ParseException.class, USAGE_MESSAGE, () -> {
            addressBookParser.parseCommand("findproject \t ");
        });
    }
}
