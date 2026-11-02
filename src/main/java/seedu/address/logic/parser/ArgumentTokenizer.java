package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tokenizes arguments string of the form: {@code preamble <prefix>value <prefix>value ...}<br>
 * e.g. {@code some preamble text t/ 11.00 t/12.00 k/ m/ July}  where prefixes are {@code t/ k/ m/}.<br>
 * 1. An argument's value can be an empty string e.g. the value of {@code k/} in the above example.<br>
 * 2. Leading and trailing whitespaces of an argument value will be discarded.<br>
 * 3. An argument may be repeated and all its values will be accumulated e.g. the value of {@code t/}
 * in the above example.<br>
 */
public class ArgumentTokenizer {

    public static final String MESSAGE_UNCLOSED_QUOTE = "Quoted field values must have a closing double quote.";
    public static final String MESSAGE_INVALID_QUOTED_VALUE =
            "A quoted field value must be followed by another argument prefix or the end of the command.";

    /**
     * Tokenizes arguments and maps the recognized prefixes to their values.
     *
     * @param argsString arguments containing optional quoted field values
     * @param prefixes   prefixes recognized by this command
     * @return the prefixes and their argument values
     * @throws ParseException if a quoted field value is not correctly delimited
     */
    public static ArgumentMultimap tokenize(String argsString, Prefix... prefixes) throws ParseException {
        List<PrefixPosition> positions = findAllPrefixPositions(argsString, prefixes);
        return extractArguments(argsString, positions);
    }

    /**
     * Finds all zero-based prefix positions in the given arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to find in the arguments string
     * @return List of zero-based prefix positions in the given arguments string
     */
    private static List<PrefixPosition> findAllPrefixPositions(String argsString, Prefix... prefixes)
            throws ParseException {
        List<PrefixPosition> positions = Arrays
                .stream(prefixes)
                .flatMap(prefix -> findPrefixPositions(argsString, prefix).stream())
                .sorted(Comparator.comparingInt(PrefixPosition::getStartPosition))
                .toList();
        List<PrefixPosition> result = new ArrayList<>();
        int quotedEnd = -1;
        for (PrefixPosition position : positions) {
            if (position.getStartPosition() < quotedEnd) {
                continue;
            }
            result.add(position);
            int valueStart = position.getStartPosition() + position.getPrefix().getPrefix().length();
            while (valueStart < argsString.length() && argsString.charAt(valueStart) <= ' ') {
                valueStart++;
            }
            if (valueStart < argsString.length() && argsString.charAt(valueStart) == '"') {
                quotedEnd = readQuotedValue(argsString, valueStart).endIndex();
            }
        }
        return result;
    }

    /**
     * @see #findAllPrefixPositions(String, Prefix...)
     */
    private static List<PrefixPosition> findPrefixPositions(String argsString, Prefix prefix) {
        List<PrefixPosition> positions = new ArrayList<>();

        int prefixPosition = findPrefixPosition(argsString, prefix.getPrefix(), 0);
        while (prefixPosition != -1) {
            PrefixPosition extendedPrefix = new PrefixPosition(prefix, prefixPosition);
            positions.add(extendedPrefix);
            prefixPosition = findPrefixPosition(argsString, prefix.getPrefix(), prefixPosition + 1);
        }

        return positions;
    }

    /**
     * Returns the index of the first occurrence of {@code prefix} in
     * {@code argsString} starting from index {@code fromIndex}. An occurrence
     * is valid if there is a whitespace before {@code prefix}. Returns -1 if no
     * such occurrence can be found.
     * <p>
     * E.g if {@code argsString} = "e/hip/900", {@code prefix} = "p/" and
     * {@code fromIndex} = 0, this method returns -1 as there are no valid
     * occurrences of "p/" with whitespace before it. However, if
     * {@code argsString} = "e/hi p/900", {@code prefix} = "p/" and
     * {@code fromIndex} = 0, this method returns 5.
     */
    private static int findPrefixPosition(String argsString, String prefix, int fromIndex) {
        int prefixIndex = argsString.indexOf(prefix, fromIndex);
        while (prefixIndex != -1) {
            if (prefixIndex > 0 && isHorizontalWhitespace(argsString.charAt(prefixIndex - 1))) {
                return prefixIndex;
            }
            prefixIndex = argsString.indexOf(prefix, prefixIndex + 1);
        }
        return -1;
    }

    /**
     * Returns whether {@code character} is a supported horizontal argument separator.
     */
    private static boolean isHorizontalWhitespace(char character) {
        return character == ' ' || character == '\t';
    }

    /**
     * Extracts prefixes and their argument values, and returns an {@code ArgumentMultimap} object that maps the
     * extracted prefixes to their respective arguments. Prefixes are extracted based on their zero-based positions in
     * {@code argsString}.
     *
     * @param argsString      Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixPositions Zero-based positions of all prefixes in {@code argsString}
     * @return ArgumentMultimap object that maps prefixes to their arguments
     */
    private static ArgumentMultimap extractArguments(String argsString, List<PrefixPosition> prefixPositions)
            throws ParseException {

        // Sort by start position
        prefixPositions.sort((prefix1, prefix2) -> prefix1.getStartPosition() - prefix2.getStartPosition());

        // Insert a PrefixPosition to represent the preamble
        PrefixPosition preambleMarker = new PrefixPosition(new Prefix(""), 0);
        prefixPositions.addFirst(preambleMarker);

        // Add a dummy PrefixPosition to represent the end of the string
        PrefixPosition endPositionMarker = new PrefixPosition(new Prefix(""), argsString.length());
        prefixPositions.add(endPositionMarker);

        // Map prefixes to their argument values (if any)
        ArgumentMultimap argMultimap = new ArgumentMultimap();
        for (int i = 0; i < prefixPositions.size() - 1; i++) {
            // Extract and store prefixes and their arguments
            Prefix argPrefix = prefixPositions
                .get(i)
                .getPrefix();
            String argValue = extractArgumentValue(argsString, prefixPositions.get(i), prefixPositions.get(i + 1));
            argMultimap.put(argPrefix, argValue);
        }

        return argMultimap;
    }

    /**
     * Returns the trimmed value of the argument in the arguments string specified by {@code currentPrefixPosition}.
     * The end position of the value is determined by {@code nextPrefixPosition}.
     */
    private static String extractArgumentValue(String argsString,
                                               PrefixPosition currentPrefixPosition,
                                               PrefixPosition nextPrefixPosition) throws ParseException {
        Prefix prefix = currentPrefixPosition.getPrefix();

        int valueStartPos = currentPrefixPosition.getStartPosition() + prefix
            .getPrefix()
            .length();
        String value = argsString.substring(valueStartPos, nextPrefixPosition.getStartPosition()).trim();

        if (prefix.getPrefix().isEmpty() || !value.startsWith("\"")) {
            return value;
        }
        QuotedValue quotedValue = readQuotedValue(value, 0);
        if (quotedValue.endIndex() != value.length()) {
            throw new ParseException(MESSAGE_INVALID_QUOTED_VALUE);
        }
        return quotedValue.value().trim();
    }

    /**
     * Reads a double-quoted value, decoding escaped double quotes and backslashes.
     */
    private static QuotedValue readQuotedValue(String text, int openingQuote) throws ParseException {
        StringBuilder value = new StringBuilder();
        for (int index = openingQuote + 1; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '"') {
                return new QuotedValue(value.toString(), index + 1);
            }
            if (character == '\\' && index + 1 < text.length()
                    && (text.charAt(index + 1) == '"' || text.charAt(index + 1) == '\\')) {
                value.append(text.charAt(++index));
            } else {
                value.append(character);
            }
        }
        throw new ParseException(MESSAGE_UNCLOSED_QUOTE);
    }

    /**
     * A decoded value and the index immediately after its closing quote.
     */
    private record QuotedValue(String value, int endIndex) {
    }

    /**
     * Represents a prefix's position in an arguments string.
     */
    private static class PrefixPosition {
        private final int startPosition;
        private final Prefix prefix;

        PrefixPosition(Prefix prefix, int startPosition) {
            this.prefix = prefix;
            this.startPosition = startPosition;
        }

        int getStartPosition() {
            return startPosition;
        }

        Prefix getPrefix() {
            return prefix;
        }
    }

}
