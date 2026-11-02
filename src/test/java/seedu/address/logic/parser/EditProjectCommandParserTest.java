package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditProjectCommand;
import seedu.address.logic.commands.EditProjectCommand.EditProjectDescriptor;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.ProjectName;

/**
 * Tests parsing of Edit Project arguments.
 */
public class EditProjectCommandParserTest {

    private final EditProjectCommandParser parser = new EditProjectCommandParser();

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_allFieldsPresent_success() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("CS2103T Team Project"));
        descriptor.setDeadline(new Deadline(LocalDate.of(2026, 11, 13)));
        descriptor.setDescription(new Description("Build a contact manager"));

        assertParsedCommand(
            " 1 n/CS2103T Team Project d/2026-11-13"
                + " desc/Build a contact manager",
            descriptor);
    }

    @Test
    public void parse_differentOrderAndWhitespace_success() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("CS2103T Team Project"));
        descriptor.setDeadline(new Deadline(LocalDate.of(2026, 11, 13)));
        descriptor.setDescription(new Description("Build a contact manager"));

        assertParsedCommand(
            " \t1 \t desc/  Build a contact manager  "
                + " d/ 2026-11-13  n/  CS2103T   Team Project  ",
            descriptor);
    }

    @Test
    public void parse_nameOnly_success() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Updated Project"));

        assertParsedCommand(" 1 n/Updated Project", descriptor);
    }

    @Test
    public void parse_deadlineOnly_success() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setDeadline(new Deadline(LocalDate.of(2027, 1, 1)));

        assertParsedCommand(" 1 d/2027-01-01", descriptor);
    }

    @Test
    public void parse_descriptionOnly_success() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setDescription(new Description("Updated description"));

        assertParsedCommand(" 1 desc/Updated description", descriptor);
    }

    @Test
    public void parse_emptyDescription_clearsDescription() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setDescription(Description.EMPTY);

        assertParsedCommand(" 1 desc/", descriptor);
        assertParsedCommand(" 1 desc/   ", descriptor);
    }

    @Test
    public void parse_secondIndex_success() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Updated Project"));

        assertParseSuccess(parser, " 2 n/Updated Project",
            new EditProjectCommand(Index.fromOneBased(2), descriptor));
    }

    @Test
    public void parse_missingOrInvalidIndex_failure() {
        String expectedMessage = String.format(
            MESSAGE_INVALID_COMMAND_FORMAT, EditProjectCommand.MESSAGE_USAGE);
        String[] invalidInputs = {
            "",
            " n/Updated Project",
            " 0 n/Updated Project",
            " -1 n/Updated Project",
            " 1.5 n/Updated Project",
            " abc n/Updated Project",
            " 2147483648 n/Updated Project",
            " 1 extra n/Updated Project"
        };

        for (String input : invalidInputs) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }

    @Test
    public void parse_noFields_failure() {
        assertParseFailure(parser, " 1", EditProjectCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, " 1   ", EditProjectCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        assertParseFailure(parser,
            " 1 n/First n/Second",
            Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser,
            " 1 n/First n/First",
            Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser,
            " 1 d/2027-01-01 d/2027-01-02",
            Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEADLINE));
        assertParseFailure(parser,
            " 1 desc/First desc/Second",
            Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DESCRIPTION));
    }

    @Test
    public void parse_emptyNameOrDeadline_failure() {
        assertParseFailure(parser, " 1 n/", "PROJECT_NAME cannot be empty.");
        assertParseFailure(parser, " 1 d/", "DEADLINE cannot be empty.");
    }

    @Test
    public void parse_invalidName_failure() {
        String[] invalidNames = {"CS2103T@", "---", "a".repeat(51)};

        for (String name : invalidNames) {
            assertParseFailure(parser, " 1 n/" + name,
                ProjectName.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidDeadline_failure() {
        String[] invalidDeadlines = {
            "2026-02-29",
            "2026-04-31",
            "2026-13-01",
            "2026-11-00",
            "13/11/2026",
            "2026-1-01",
            "2026-11-13T12:00",
            "tomorrow"
        };

        for (String deadline : invalidDeadlines) {
            assertParseFailure(parser, " 1 d/" + deadline,
                ParserUtil.MESSAGE_INVALID_DEADLINE);
        }
    }

    @Test
    public void parse_pastDeadlineAndValidLeapDay_success() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setDeadline(new Deadline(LocalDate.of(2020, 1, 1)));
        assertParsedCommand(" 1 d/2020-01-01", descriptor);

        descriptor.setDeadline(new Deadline(LocalDate.of(2028, 2, 29)));
        assertParsedCommand(" 1 d/2028-02-29", descriptor);
    }

    @Test
    public void parse_nameAndDescriptionAtLengthLimits_success() {
        String name = "a".repeat(50);
        String description = "b".repeat(200);
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName(name));
        descriptor.setDescription(new Description(description));

        assertParsedCommand(" 1 n/" + name + " desc/" + description, descriptor);
    }

    @Test
    public void parse_descriptionTooLong_failure() {
        assertParseFailure(parser, " 1 desc/" + "a".repeat(201),
            Description.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_lineBreaks_failure() {
        String expectedMessage = String.format(
            MESSAGE_INVALID_COMMAND_FORMAT, EditProjectCommand.MESSAGE_USAGE);
        String[] lineBreaks = {"\r", "\n", "\u0085", "\u2028", "\u2029"};

        for (String lineBreak : lineBreaks) {
            assertParseFailure(parser,
                " 1 desc/First" + lineBreak + "Second", expectedMessage);
            assertParseFailure(parser,
                " 1 desc/Description" + lineBreak, expectedMessage);
            assertParseFailure(parser,
                " 1 n/First" + lineBreak + "Second", expectedMessage);
        }
    }

    @Test
    public void parseCommand_editProject_routesToCorrectParser() throws Exception {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Updated Project"));

        assertEquals(
            new EditProjectCommand(Index.fromOneBased(1), descriptor),
            new AddressBookParser().parseCommand(
                "editproject 1 n/Updated Project"));
    }

    /**
     * Verifies that parsing produces the expected command for project index 1.
     */
    private void assertParsedCommand(String input, EditProjectDescriptor descriptor) {
        assertParseSuccess(parser, input,
            new EditProjectCommand(Index.fromOneBased(1), descriptor));
    }

    @Test
    public void parse_unicodeWhitespaceOnlyDescription_clearsDescription() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setDescription(Description.EMPTY);

        String[] blankValues = {"\u2003", "\u00A0", "\u2007", "\u202F", "\u3000", " \u2003\u00A0 "};
        for (String value : blankValues) {
            assertParsedCommand(" 1 desc/" + value, descriptor);
            assertParsedCommand(" 1 desc/\"" + value + "\"", descriptor);
        }
    }
}
