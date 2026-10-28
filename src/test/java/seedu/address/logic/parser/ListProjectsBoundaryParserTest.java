package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ListProjectsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Tests optional contact lookup syntax through the command dispatcher. */
public class ListProjectsBoundaryParserTest {
    @TestFactory
    public Stream<DynamicTest> parse_validBoundsAndWhitespace_returnsExpectedCommand() {
        return Stream.of("1", "2", "2147483646", "2147483647", "0001")
                .flatMap(value -> Stream.of(" ", "   ", "\t").map(separator -> dynamicTest(
                        "index " + value + " separator " + separator.length() + ":" + (int) separator.charAt(0), () -> {
                            assertEquals(new ListProjectsCommand(Index.fromOneBased(Integer.parseInt(value))),
                                    new AddressBookParser().parseCommand("listprojects" + separator + "c/" + value));
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> parse_noArguments_preservesOriginalCommand() {
        return Stream.of("", " ", "   ", "\t", " \t ").map(suffix -> dynamicTest("blank " + suffix, () -> {
            assertEquals(new ListProjectsCommand(), new AddressBookParser().parseCommand("listprojects" + suffix));
        }));
    }

    @TestFactory
    public Stream<DynamicTest> parse_invalidPartitions_rejectsWithUsage() {
        return Stream.of("c/", "c/ ", "c/0", "c/00", "c/-1", "c/-2147483648", "c/+1", "c/2147483648",
                "c/999999999999999999", "c/1.0", "c/1e0", "c/1 2", "c/1\t2", "c/abc", "c/1_0",
                "c/0x1", "c/1/2", "c/\u200b1", "c/1\u200b", "1", "C/1", "c /1", "contact/1",
                "pr/1", "unexpected c/1", "c/1 unexpected", "x/1 c/1", "c/1 x/1", "c/1pr/1")
                .map(args -> dynamicTest(args, () -> {
                    ParseException error = assertThrows(ParseException.class, () ->
                            new AddressBookParser().parseCommand("listprojects " + args));
                    String expected = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                            ListProjectsCommand.MESSAGE_USAGE);
                    assertEquals(expected, error.getMessage());
                }));
    }

    @TestFactory
    public Stream<DynamicTest> parse_duplicatePrefixPartitions_rejectsWithoutAcceptingLastValue() {
        return Stream.of("", "1", "2", "invalid").flatMap(first -> Stream.of("", "1", "2", "invalid")
                .map(second -> dynamicTest("duplicate " + first + " then " + second, () -> {
                    ParseException error = assertThrows(ParseException.class, () -> new AddressBookParser()
                            .parseCommand("listprojects c/" + first + " c/" + second));
                    assertEquals(Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTACT), error.getMessage());
                })));
    }
}
