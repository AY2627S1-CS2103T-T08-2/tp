package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class ArgumentTokenizerTest {

    private final Prefix unknownPrefix = new Prefix("--u");
    private final Prefix pSlash = new Prefix("p/");
    private final Prefix dashT = new Prefix("-t");
    private final Prefix hatQ = new Prefix("^Q");

    @Test
    public void tokenize_emptyArgsString_noValues() throws ParseException {
        String argsString = "  ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);

        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);
    }

    private void assertPreamblePresent(ArgumentMultimap argMultimap, String expectedPreamble) {
        assertEquals(expectedPreamble, argMultimap.getPreamble());
    }

    private void assertPreambleEmpty(ArgumentMultimap argMultimap) {
        assertTrue(argMultimap
                .getPreamble()
                .isEmpty());
    }

    /**
     * Asserts all the arguments in {@code argMultimap} with {@code prefix} match the {@code expectedValues}
     * and only the last value is returned upon calling {@code ArgumentMultimap#getValue(Prefix)}.
     */
    private void assertArgumentPresent(ArgumentMultimap argMultimap, Prefix prefix, String... expectedValues) {

        // Verify the last value is returned
        assertEquals(expectedValues[expectedValues.length - 1], argMultimap
                .getValue(prefix)
                .get());

        // Verify the number of values returned is as expected
        assertEquals(expectedValues.length, argMultimap
                .getAllValues(prefix)
                .size());

        // Verify all values returned are as expected and in order
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], argMultimap
                    .getAllValues(prefix)
                    .get(i));
        }
    }

    private void assertArgumentAbsent(ArgumentMultimap argMultimap, Prefix prefix) {
        assertFalse(argMultimap
                .getValue(prefix)
                .isPresent());
    }

    @Test
    public void tokenize_noPrefixes_allTakenAsPreamble() throws ParseException {
        String argsString = "  some random string /t tag with leading and trailing spaces ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString);

        // Same string expected as preamble, but leading/trailing spaces should be trimmed
        assertPreamblePresent(argMultimap, argsString.trim());

    }

    @Test
    public void tokenize_oneArgument() throws ParseException {
        // Preamble present
        String argsString = "  Some preamble string p/ Argument value ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreamblePresent(argMultimap, "Some preamble string");
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

        // No preamble
        argsString = " p/   Argument value ";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreambleEmpty(argMultimap);
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

    }

    @Test
    public void tokenize_multipleArguments() throws ParseException {
        // Only two arguments are present
        String argsString = "SomePreambleString -t dashT-Value p/pSlash value";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentAbsent(argMultimap, hatQ);

        // All three arguments are present
        argsString = "Different Preamble String ^Q111 -t dashT-Value p/pSlash value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "Different Preamble String");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentPresent(argMultimap, hatQ, "111");

        /* Also covers: Reusing of the tokenizer multiple times */

        // Reuse tokenizer on an empty string to ensure ArgumentMultimap is correctly reset
        // (i.e. no stale values from the previous tokenizing remain)
        argsString = "";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);

        /* Also covers: testing for prefixes not specified as a prefix */

        // Prefixes not previously given to the tokenizer should not return any values
        argsString = unknownPrefix + "some value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertArgumentAbsent(argMultimap, unknownPrefix);
        assertPreamblePresent(argMultimap, argsString); // Unknown prefix is taken as part of preamble
    }

    @Test
    public void tokenize_horizontalWhitespaceBeforePrefix_recognizesSpacesAndTabs() throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" p/first\t-tsecond", pSlash, dashT);

        assertArgumentPresent(argMultimap, pSlash, "first");
        assertArgumentPresent(argMultimap, dashT, "second");

        argMultimap = ArgumentTokenizer.tokenize(" p/first  -tsecond", pSlash, dashT);

        assertArgumentPresent(argMultimap, pSlash, "first");
        assertArgumentPresent(argMultimap, dashT, "second");
    }

    @Test
    public void tokenize_nonHorizontalSeparatorBeforePrefix_doesNotRecognizePrefix() throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" p/first\n-tsecond", pSlash, dashT);

        assertArgumentPresent(argMultimap, pSlash, "first\n-tsecond");
        assertArgumentAbsent(argMultimap, dashT);
    }

    @Test
    public void tokenize_multipleArgumentsWithRepeats() throws ParseException {
        // Two arguments repeated, some have empty values
        String argsString = "SomePreambleString -t dashT-Value ^Q ^Q -t another dashT value p/ pSlash value -t";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value", "another dashT value", "");
        assertArgumentPresent(argMultimap, hatQ, "", "");
    }

    @Test
    public void tokenize_multipleArgumentsJoined() throws ParseException {
        String argsString = "SomePreambleStringp/ pSlash joined-tjoined -t not joined^Qjoined";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleStringp/ pSlash joined-tjoined");
        assertArgumentAbsent(argMultimap, pSlash);
        assertArgumentPresent(argMultimap, dashT, "not joined^Qjoined");
        assertArgumentAbsent(argMultimap, hatQ);
    }

    @Test
    public void equalsMethod() {
        Prefix aaa = new Prefix("aaa");

        assertEquals(aaa, aaa);
        assertEquals(new Prefix("aaa"), aaa);

        assertNotEquals("aaa", aaa);
        assertNotEquals(new Prefix("aab"), aaa);
    }

    @Test
    public void tokenize_quotedName_doesNotTreatItsContentsAsPrefixes() throws ParseException {
        Prefix name = new Prefix("n/");
        Prefix address = new Prefix("a/");
        ArgumentMultimap result = ArgumentTokenizer.tokenize(
                "1 n/\"Arjun a/l Kumar\" a/Block 1", name, address);

        assertPreamblePresent(result, "1");
        assertArgumentPresent(result, name, "Arjun a/l Kumar");
        assertArgumentPresent(result, address, "Block 1");
    }

    @Test
    public void tokenize_quotedValueAtEnd_keepsLiteralPrefixes() throws ParseException {
        ArgumentMultimap result = ArgumentTokenizer.tokenize(" p/\"first -tsecond\"", pSlash, dashT);

        assertArgumentPresent(result, pSlash, "first -tsecond");
        assertArgumentAbsent(result, dashT);
    }

    @Test
    public void tokenize_quotedValueWithEscapes_decodesQuotesAndBackslashes() throws ParseException {
        ArgumentMultimap result = ArgumentTokenizer.tokenize(
                " p/\"Office \\\"A\\\" C:\\\\Contacts\"", pSlash);

        assertArgumentPresent(result, pSlash, "Office \"A\" C:\\Contacts");
    }

    @Test
    public void tokenize_quotesInsideUnquotedValue_preservesQuotes() throws ParseException {
        ArgumentMultimap result = ArgumentTokenizer.tokenize(" p/Office \"A\"", pSlash);

        assertArgumentPresent(result, pSlash, "Office \"A\"");
    }

    @Test
    public void tokenize_quotedEmptyValue_returnsEmptyValue() throws ParseException {
        ArgumentMultimap result = ArgumentTokenizer.tokenize(" p/\"\"", pSlash);

        assertArgumentPresent(result, pSlash, "");
    }

    @Test
    public void tokenize_repeatedPrefixOutsideQuotes_preservesBothValues() throws ParseException {
        ArgumentMultimap result = ArgumentTokenizer.tokenize(" p/\"first p/inside\" p/second", pSlash);

        assertArgumentPresent(result, pSlash, "first p/inside", "second");
    }

    @Test
    public void tokenize_unclosedQuote_throwsParseException() {
        assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTE, () ->
                ArgumentTokenizer.tokenize(" p/\"first -tsecond", pSlash, dashT));
    }

    @Test
    public void tokenize_textAfterClosingQuote_throwsParseException() {
        assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_INVALID_QUOTED_VALUE, () ->
                ArgumentTokenizer.tokenize(" p/\"first\" extra -tsecond", pSlash, dashT));
        assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_INVALID_QUOTED_VALUE, () ->
                ArgumentTokenizer.tokenize(" p/\"first\"-tsecond", pSlash, dashT));
    }
}
