package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ListMembersCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.ProjectName;

public class ListMembersCommandParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_validProjectName_returnsListMembersCommand() throws Exception {
        ListMembersCommand command = (ListMembersCommand) parser.parseCommand(
            "listmembers pr/CS2103T Team Project");

        assertEquals(new ListMembersCommand(new ProjectName("CS2103T Team Project")), command);
    }

    @Test
    public void parseCommand_missingProjectName_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListMembersCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("listmembers"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("listmembers extra"));
    }

    @Test
    public void parseCommand_duplicateProjectPrefix_throwsParseException() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PROJECT);
        String command = "listmembers pr/CS2103T pr/Orbital";
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(command));
    }

    @Test
    public void parseCommand_emptyProjectName_throwsParseException() {
        String command = "listmembers pr/";
        assertThrows(ParseException.class, "PROJECT_NAME cannot be empty.", () -> parser.parseCommand(command));
    }

    @Test
    public void parseCommand_unexpectedPreamble_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListMembersCommand.MESSAGE_USAGE);
        String command = "listmembers extra pr/CS2103T";
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(command));
    }
}
