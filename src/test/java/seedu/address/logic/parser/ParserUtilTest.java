package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.ProjectName;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }

    @Test
    public void parseProjectName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseProjectName(null));
    }

    @Test
    public void parseProjectName_invalidValue_throwsParseException() {
        String[] invalidNames = {"", "   ", "---", "Project@", "a".repeat(51)};

        for (String name : invalidNames) {
            assertThrows(ParseException.class, () -> ParserUtil.parseProjectName(name));
        }
    }

    @Test
    public void parseProjectName_validValue_returnsNormalisedName() throws Exception {
        ProjectName result = ParserUtil.parseProjectName("  CS2103T   Team Project  ");

        // Check the display value because equals() ignores case and spacing.
        assertEquals("CS2103T Team Project", result.toString());
    }

    @Test
    public void parseProjectName_maximumLength_success() throws Exception {
        String name = "a".repeat(50);

        assertEquals(new ProjectName(name), ParserUtil.parseProjectName(name));
    }

    @Test
    public void parseDeadline_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDeadline(null));
    }

    @Test
    public void parseDeadline_invalidValue_throwsParseException() {
        String[] invalidDeadlines = {
            "",
            "   ",
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
            assertThrows(ParseException.class, () -> ParserUtil.parseDeadline(deadline));
        }
    }

    @Test
    public void parseDeadline_validValue_returnsDeadline() throws Exception {
        assertEquals(
                new Deadline(LocalDate.of(2026, 11, 13)),
                ParserUtil.parseDeadline("  2026-11-13  "));
    }

    @Test
    public void parseDeadline_validLeapDay_returnsDeadline() throws Exception {
        assertEquals(
                new Deadline(LocalDate.of(2028, 2, 29)),
                ParserUtil.parseDeadline("2028-02-29"));
    }

    @Test
    public void parseDeadline_pastDate_returnsDeadline() throws Exception {
        assertEquals(
                new Deadline(LocalDate.of(2000, 1, 1)),
                ParserUtil.parseDeadline("2000-01-01"));
    }

    @Test
    public void parseDescription_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDescription(null));
    }

    @Test
    public void parseDescription_blank_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseDescription(""));
        assertThrows(ParseException.class, () -> ParserUtil.parseDescription("   "));
    }

    @Test
    public void parseDescription_tooLong_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseDescription("a".repeat(201)));
    }

    @Test
    public void parseDescription_lineBreaks_throwsParseException() {
        String[] lineBreaks = {"\r", "\n", "\u0085", "\u2028", "\u2029"};

        for (String lineBreak : lineBreaks) {
            assertThrows(ParseException.class, () -> ParserUtil.parseDescription("First" + lineBreak + "Second"));

            assertThrows(ParseException.class, () -> ParserUtil.parseDescription(lineBreak + "Description"));

            assertThrows(ParseException.class, () -> ParserUtil.parseDescription("Description" + lineBreak));
        }
    }

    @Test
    public void parseDescription_validValue_preservesInternalSpacingAndCase() throws Exception {
        assertEquals(
                new Description("Build  a Contact Manager!"),
                ParserUtil.parseDescription("  Build  a Contact Manager!  "));
    }

    @Test
    public void parseDescription_lengthBoundaries_success() throws Exception {
        assertEquals(new Description("a"), ParserUtil.parseDescription("a"));

        String description = "a".repeat(200);
        assertEquals(
                new Description(description),
                ParserUtil.parseDescription("  " + description + "  "));
    }
}
