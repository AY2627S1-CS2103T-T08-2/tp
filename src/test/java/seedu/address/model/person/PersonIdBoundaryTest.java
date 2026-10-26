package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/** Tests the value, syntax, and equality partitions of contact identifiers. */
public class PersonIdBoundaryTest {
    private static final String CANONICAL_ID = "01234567-89ab-cdef-0123-456789abcdef";
    private static final long[] LONG_BOUNDARIES = {
        Long.MIN_VALUE, Long.MIN_VALUE + 1, -1, 0, 1, Long.MAX_VALUE - 1, Long.MAX_VALUE
    };

    @TestFactory
    public Stream<DynamicTest> fromString_bitBoundaries_preservesUuid() {
        Stream<UUID> signedBoundaries = Arrays.stream(LONG_BOUNDARIES).boxed()
                .flatMap(first -> Arrays.stream(LONG_BOUNDARIES).mapToObj(second -> new UUID(first, second)));
        Stream<UUID> individualBits = IntStream.range(0, 128)
                .mapToObj(bit -> bit < 64 ? new UUID(1L << bit, 0) : new UUID(0, 1L << (bit - 64)));
        return Stream.concat(signedBoundaries, individualBits).distinct().map(uuid ->
                dynamicTest(uuid.toString(), () -> {
                    PersonId constructed = new PersonId(uuid);
                    PersonId parsed = PersonId.fromString(uuid.toString());
                    assertEquals(uuid, constructed.getId());
                    assertEquals(uuid, parsed.getId());
                    assertEquals(constructed, parsed);
                    assertEquals(uuid.toString(), parsed.toString());
                }));
    }

    @TestFactory
    public Stream<DynamicTest> fromString_hexadecimalPositions_acceptsAsciiHex() {
        return IntStream.range(0, CANONICAL_ID.length()).filter(position -> CANONICAL_ID.charAt(position) != '-')
                .mapToObj(position -> dynamicTest("hexadecimal position " + position, () -> {
                    for (char digit : "0123456789abcdefABCDEF".toCharArray()) {
                        String text = replaceCharacter(CANONICAL_ID, position, digit);
                        PersonId parsed = PersonId.fromString(text);
                        assertEquals(UUID.fromString(text), parsed.getId(), text);
                        assertEquals(text.toLowerCase(Locale.ROOT), parsed.toString(), text);
                    }
                }));
    }

    @TestFactory
    public Stream<DynamicTest> fromString_nonHexadecimalPositions_rejectsInput() {
        return IntStream.range(0, CANONICAL_ID.length()).filter(position -> CANONICAL_ID.charAt(position) != '-')
                .mapToObj(position -> dynamicTest("invalid hexadecimal position " + position, () -> {
                    char[] invalidDigits = {'g', 'G', '_', '+', '-', ' ', '\t', '\n', '\0', '\u0661', '\uff11'};
                    for (char digit : invalidDigits) {
                        String text = replaceCharacter(CANONICAL_ID, position, digit);
                        assertThrows(IllegalArgumentException.class, () -> PersonId.fromString(text));
                    }
                }));
    }

    @TestFactory
    public Stream<DynamicTest> fromString_separatorPositions_rejectsReplacementOrRemoval() {
        return IntStream.of(8, 13, 18, 23).mapToObj(position -> dynamicTest("separator " + position, () -> {
            for (char separator : new char[] {'_', ':', ' ', 'a', '\u2010', '\uff0d', '\0'}) {
                String text = replaceCharacter(CANONICAL_ID, position, separator);
                assertThrows(IllegalArgumentException.class, () -> PersonId.fromString(text));
            }
            String missingSeparator = CANONICAL_ID.substring(0, position) + CANONICAL_ID.substring(position + 1);
            assertThrows(IllegalArgumentException.class, () -> PersonId.fromString(missingSeparator));
        }));
    }

    @TestFactory
    public Stream<DynamicTest> fromString_segmentWidthBoundaries_rejectsNonCanonicalWidths() {
        String[] canonicalSegments = CANONICAL_ID.split("-");
        return IntStream.range(0, canonicalSegments.length).boxed().flatMap(segment -> {
            int width = canonicalSegments[segment].length();
            return IntStream.of(0, 1, width - 1, width + 1, width + 2).distinct().mapToObj(length ->
                    dynamicTest("segment " + segment + " length " + length, () -> {
                        String[] segments = canonicalSegments.clone();
                        segments[segment] = "0".repeat(length);
                        String text = String.join("-", segments);
                        assertThrows(IllegalArgumentException.class, () -> PersonId.fromString(text));
                    }));
        });
    }

    @TestFactory
    public Stream<DynamicTest> fromString_wrappersAndWhitespace_rejectsInput() {
        List<String> invalidInputs = new ArrayList<>(List.of("", " ", "not-a-uuid", "1-1-1-1-1",
                CANONICAL_ID.replace("-", ""), "{" + CANONICAL_ID + "}", "urn:uuid:" + CANONICAL_ID,
                CANONICAL_ID + "-0", CANONICAL_ID + "0", "0".repeat(1024)));
        for (String whitespace : List.of(" ", "\t", "\r", "\n", "\r\n", "\u00a0", "\u2003", "\u2028", "\u2029")) {
            invalidInputs.add(whitespace + CANONICAL_ID);
            invalidInputs.add(CANONICAL_ID + whitespace);
        }
        return IntStream.range(0, invalidInputs.size()).mapToObj(index ->
                dynamicTest("invalid envelope " + index, () -> {
                    assertThrows(IllegalArgumentException.class, () -> PersonId.fromString(invalidInputs.get(index)));
                }));
    }

    @Test
    public void equals_caseVariants_workAsHashKeysAndPreserveTypeBoundaries() {
        PersonId lower = PersonId.fromString(CANONICAL_ID);
        PersonId upper = PersonId.fromString(CANONICAL_ID.toUpperCase(Locale.ROOT));
        PersonId reconstructed = new PersonId(lower.getId());
        Map<PersonId, String> values = new HashMap<>();
        values.put(lower, "contact");

        assertEquals(lower, upper);
        assertEquals(upper, reconstructed);
        assertEquals(lower, reconstructed);
        assertEquals("contact", values.get(upper));
        assertEquals("contact", values.get(reconstructed));
        assertEquals(1, new HashSet<>(List.of(lower, upper, reconstructed)).size());
        assertFalse(lower.equals(lower.getId()));
        assertFalse(lower.equals(CANONICAL_ID));
        assertFalse(lower.equals(null));
    }

    @Test
    public void generate_sample_hasDistinctVersionFourVariantTwoIds() {
        Set<PersonId> ids = new HashSet<>();
        for (int index = 0; index < 256; index++) {
            PersonId generated = PersonId.generate();
            assertEquals(4, generated.getId().version());
            assertEquals(2, generated.getId().variant());
            assertEquals(generated, PersonId.fromString(generated.toString()));
            assertTrue(ids.add(generated), "Generated a duplicate identifier in the sample");
        }
    }

    private String replaceCharacter(String text, int position, char replacement) {
        return text.substring(0, position) + replacement + text.substring(position + 1);
    }
}
