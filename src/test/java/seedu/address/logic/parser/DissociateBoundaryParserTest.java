package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.DissociateCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Exercises dissociation syntax through the application's command dispatcher. */
public class DissociateBoundaryParserTest {
    private static final String USAGE = String.format(
            Messages.MESSAGE_INVALID_COMMAND_FORMAT, DissociateCommand.MESSAGE_USAGE);

    @TestFactory
    public Stream<DynamicTest> parse_validIntegerBoundaries_acceptsBothPrefixOrders() {
        return Stream.of("1", "2", "2147483646", "2147483647", "01", "00000000000000000000001")
                .flatMap(value -> IntStream.range(0, 2).boxed().flatMap(field -> IntStream.range(0, 2)
                        .mapToObj(order -> dynamicTest("valid " + value + " field " + field + " order " + order, () -> {
                            String contact = field == 0 ? value : "1";
                            String project = field == 1 ? value : "1";
                            assertEquals(new DissociateCommand(Index.fromOneBased(Integer.parseInt(contact)),
                                    Index.fromOneBased(Integer.parseInt(project))), new AddressBookParser()
                                    .parseCommand(command(contact, project, order)));
                        }))));
    }

    @TestFactory
    public Stream<DynamicTest> parse_invalidIntegerPartitions_rejectsWithoutUncheckedExceptions() {
        return Stream.of("", " ", "0", "00", "-0", "-1", "-2147483648", "-2147483649", "+1",
                "2147483648", "99999999999999999999", "1.0", "1.5", "1e0", "0x1", "one", "1 2",
                "1\t2", "1\n2", "1_0", "1/2", "1x", "x1", "\u200b1", "1\u200b")
                .flatMap(value -> IntStream.range(0, 2).boxed().flatMap(field -> IntStream.range(0, 2)
                        .mapToObj(order -> dynamicTest(
                                "invalid " + value + " field " + field + " order " + order, () -> {
                                    String contact = field == 0 ? value : "1";
                                    String project = field == 1 ? value : "1";
                                    AddressBookParser parser = new AddressBookParser();
                                    ParseException error = assertThrows(ParseException.class, () ->
                                            parser.parseCommand(command(contact, project, order)));
                                    if (!value.contains("\n")) {
                                        assertEquals(USAGE, error.getMessage());
                                    }
                                }))));
    }

    @TestFactory
    public Stream<DynamicTest> parse_duplicatePrefixesAtEveryPosition_rejectsEvenIdenticalValues() {
        return Stream.of("", "1", "2", "invalid").flatMap(value -> IntStream.range(0, 2).boxed()
                .flatMap(field -> IntStream.range(0, 3).mapToObj(position -> dynamicTest(
                        "duplicate value " + value + " field " + field + " position " + position, () -> {
                            var prefix = field == 0 ? PREFIX_CONTACT : PREFIX_PROJECT;
                            var parts = new java.util.ArrayList<>(List.of("c/1", "pr/1"));
                            parts.add(position, prefix + value);
                            ParseException error = assertThrows(ParseException.class, () -> new AddressBookParser()
                                    .parseCommand("dissociate " + String.join(" ", parts)));
                            assertEquals(Messages.getErrorMessageForDuplicatePrefixes(prefix), error.getMessage());
                        }))));
    }

    @TestFactory
    public Stream<DynamicTest> parse_unsupportedArgumentsAtEveryPosition_rejects() {
        return Stream.of("x/1", "r/Writer", "n/Name", "unexpected", "C/1", "PR/1", "c /1", "pr /1")
                .flatMap(token -> IntStream.range(0, 3).mapToObj(position -> dynamicTest(
                        "unsupported " + token + " position " + position, () -> {
                            var parts = new java.util.ArrayList<>(List.of("c/1", "pr/1"));
                            parts.add(position, token);
                            ParseException error = assertThrows(ParseException.class, () -> new AddressBookParser()
                                    .parseCommand("dissociate " + String.join(" ", parts)));
                            assertEquals(USAGE, error.getMessage());
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> parse_whitespaceBetweenPrefixes_acceptsWhitespaceSeparators() {
        List<String> separators = List.of(" ", "   ", "\t");
        return IntStream.range(0, separators.size()).boxed().flatMap(index -> IntStream.range(0, 2)
                .mapToObj(order -> dynamicTest("separator " + index + " order " + order, () -> {
                    String args = order == 0 ? "c/1" + separators.get(index) + "pr/2"
                            : "pr/2" + separators.get(index) + "c/1";
                    assertEquals(new DissociateCommand(Index.fromOneBased(1), Index.fromOneBased(2)),
                            new AddressBookParser().parseCommand("dissociate " + args));
                })));
    }

    private static String command(String contact, String project, int order) {
        return "dissociate " + (order == 0 ? "c/" + contact + " pr/" + project
                : "pr/" + project + " c/" + contact);
    }
}

