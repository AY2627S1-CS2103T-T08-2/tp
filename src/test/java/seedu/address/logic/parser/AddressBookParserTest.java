package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddProjectCommand;
import seedu.address.logic.commands.AssociateCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DissociateCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ListProjectsCommand;
import seedu.address.logic.commands.SetRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.membership.Role;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_associate() throws Exception {
        AssociateCommand expectedCommand = new AssociateCommand(INDEX_SECOND_PERSON, Index.fromOneBased(1));

        assertEquals(expectedCommand, parser.parseCommand("associate c/2 pr/1"));
        assertEquals(expectedCommand, parser.parseCommand("associate pr/1 c/2"));
        assertEquals(expectedCommand, parser.parseCommand("associate c/2\tpr/1"));
        assertEquals(expectedCommand, parser.parseCommand("associate pr/1\tc/2"));
    }

    @Test
    public void parseCommand_setRole() throws Exception {
        SetRoleCommand expectedCommand = new SetRoleCommand(
            Index.fromOneBased(2), Index.fromOneBased(1), new Role("Implement the parser"));

        assertEquals(expectedCommand, parser.parseCommand("setrole c/2 pr/1 r/Implement the parser"));
    }

    @Test
    public void parseCommand_associateInvalidArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AssociateCommand.MESSAGE_USAGE);

        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("associate"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("associate c/2 pr/0"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("associate c/2 pr/1 x/3"));
    }

    @Test
    public void parseCommand_associateDuplicatePrefix_throwsParseException() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTACT);

        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("associate c/1 c/2 pr/1"));
    }

    @Test
    public void parseCommand_dissociate() throws Exception {
        DissociateCommand expectedCommand = new DissociateCommand(INDEX_SECOND_PERSON, Index.fromOneBased(1));

        assertEquals(expectedCommand, parser.parseCommand("dissociate c/2 pr/1"));
        assertEquals(expectedCommand, parser.parseCommand("dissociate pr/1 c/2"));
        assertEquals(expectedCommand, parser.parseCommand("dissociate c/2\tpr/1"));
        assertEquals(expectedCommand, parser.parseCommand("dissociate pr/1\tc/2"));
    }

    @Test
    public void parseCommand_dissociateInvalidArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DissociateCommand.MESSAGE_USAGE);
        String[] invalidCommands = {
            "dissociate", "dissociate c/2", "dissociate pr/1", "dissociate c/0 pr/1",
            "dissociate c/2 pr/0", "dissociate c/2 proj/1", "dissociate c/2 pr/1 x/3"
        };

        for (String command : invalidCommands) {
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(command));
        }
    }

    @Test
    public void parseCommand_dissociateDuplicateContactPrefix_throwsParseException() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTACT);

        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("dissociate c/1 c/2 pr/1"));
    }

    @Test
    public void parseCommand_dissociateDuplicateProjectPrefix_throwsParseException() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PROJECT);

        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("dissociate c/2 pr/1 pr/2"));
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(" \t" + ClearCommand.COMMAND_WORD + " \t") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
            DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
            + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(" \t" + ExitCommand.COMMAND_WORD + " \t") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
            FindCommand.COMMAND_WORD + " " + keywords
                .stream()
                .collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(" \t" + HelpCommand.COMMAND_WORD + " \t") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(" \t" + ListCommand.COMMAND_WORD + " \t") instanceof ListCommand);
    }

    @Test
    public void parseCommand_listProjects() throws Exception {
        assertEquals(new ListProjectsCommand(), parser.parseCommand("listprojects"));
        assertEquals(new ListProjectsCommand(), parser.parseCommand(" \tlistprojects \t"));
        assertEquals(new ListProjectsCommand(INDEX_SECOND_PERSON), parser.parseCommand("listprojects c/2"));
        assertEquals(new ListProjectsCommand(INDEX_SECOND_PERSON), parser.parseCommand("  listprojects   c/ 2  "));
        assertEquals(new ListProjectsCommand(INDEX_SECOND_PERSON), parser.parseCommand("listprojects\tc/2"));
    }

    @Test
    public void parseCommand_listProjectsInvalidArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListProjectsCommand.MESSAGE_USAGE);
        String[] invalidCommands = {
            "listprojects 3", "listprojects unexpected text", "listprojects n/Alice", "listprojects\t123 \t",
            "listprojects c/", "listprojects c/0", "listprojects c/-1", "listprojects c/abc",
            "listprojects c/2147483648", "listprojects c/2 pr/1"
        };
        for (String command : invalidCommands) {
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(command));
        }
    }

    @Test
    public void parseCommand_listProjectsDuplicatePrefix_throwsParseException() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTACT);
        assertThrows(ParseException.class,
            expectedMessage, () -> parser.parseCommand("listprojects c/1 c/2"));
    }

    @Test
    public void parseCommand_parameterFreeCommandsWithExtraArguments_throwsParseException() {
        Map<String, String> usages = Map.of(
            ClearCommand.COMMAND_WORD, ClearCommand.MESSAGE_USAGE,
            ExitCommand.COMMAND_WORD, ExitCommand.MESSAGE_USAGE,
            HelpCommand.COMMAND_WORD, HelpCommand.MESSAGE_USAGE,
            ListCommand.COMMAND_WORD, ListCommand.MESSAGE_USAGE);

        usages.forEach((commandWord, usage) -> {
            String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
            for (String arguments : List.of(" 3", " unexpected text", " n/Alice", "\t123 \t")) {
                assertThrows(ParseException.class,
                    expectedMessage, () -> parser.parseCommand(commandWord + arguments));
            }
        });
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void parseCommand_addProject() throws Exception {
        AddProjectCommand command = (AddProjectCommand) parser.parseCommand(
            "addproject n/CS2103T d/2026-11-13 desc/Build a contact manager");

        Model model = new ModelManager();
        command.execute(model);

        assertEquals(1, model
            .getFilteredProjectList()
            .size());
        Project actualProject = model
            .getFilteredProjectList()
            .get(0);

        assertNotNull(actualProject.getProjectId());
        assertEquals("CS2103T", actualProject
            .getName()
            .toString());
        assertEquals(new Deadline(LocalDate.of(2026, 11, 13)), actualProject.getDeadline());
        assertEquals(new Description("Build a contact manager"), actualProject.getDescription());
    }

    @Test
    public void parseCommand_lineBreaksBeforeTrimming_throwsParseException() {
        String[] lineBreaks = {"\r", "\n", "\r\n", "\u000B", "\f", "\u0085", "\u2028", "\u2029"};

        for (String lineBreak : lineBreaks) {
            String[] commands = {
                lineBreak + "help",
                "help" + lineBreak,
                "setrole c/1 pr/1 r/" + lineBreak,
                "setrole c/1 pr/1 r/" + lineBreak + "Leader",
                "setrole c/1 pr/1 r/Lead" + lineBreak + "er",
                "setrole c/1 pr/1 r/Leader" + lineBreak,
                "addproject n/Alpha d/2026-12-01 desc/" + lineBreak + "Notes",
                "addproject n/Alpha d/2026-12-01 desc/Notes" + lineBreak,
                "editproject 1 desc/" + lineBreak,
                "editproject 1 desc/" + lineBreak + "Notes",
                "editproject 1 desc/Notes" + lineBreak
            };

            for (String command : commands) {
                assertThrows(ParseException.class,
                        "Commands must be entered on a single line.", () -> parser.parseCommand(command));
            }
        }
    }
}
