package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddProjectCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;

public class AddProjectCommandParserTest {

    private final AddProjectCommandParser parser = new AddProjectCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Project expectedProject = new Project(
                new ProjectName("CS2103T"),
                new Deadline(LocalDate.of(2026, 11, 13)),
                new Description("Build a contact manager"));

        assertProjectParseSuccess(
                " n/CS2103T d/2026-11-13 desc/Build a contact manager",
                expectedProject);
    }

    @Test
    public void parse_descriptionOmitted_success() {
        Project expectedProject = new Project(
                new ProjectName("CS2103T"),
                new Deadline(LocalDate.of(2026, 11, 13)),
                Description.EMPTY);

        assertProjectParseSuccess(
                " n/CS2103T d/2026-11-13",
                expectedProject);
    }

    @Test
    public void parse_differentParameterOrder_success() {
        Project expectedProject = new Project(
                new ProjectName("CS2103T"),
                new Deadline(LocalDate.of(2026, 11, 13)),
                new Description("Build a contact manager"));

        assertProjectParseSuccess(
                " desc/Build a contact manager d/2026-11-13 n/CS2103T",
                expectedProject);
    }

    @Test
    public void parse_extraWhitespace_success() {
        Project expectedProject = new Project(
                new ProjectName("CS2103T Team Project"),
                new Deadline(LocalDate.of(2026, 11, 13)),
                new Description("Build  a contact manager"));

        assertProjectParseSuccess(
                " n/  CS2103T   Team Project  d/ 2026-11-13 "
                        + "desc/  Build  a contact manager  ",
                expectedProject);
    }

    @Test
    public void parse_pastDeadline_success() {
        Project expectedProject = new Project(
                new ProjectName("CS2103T"),
                new Deadline(LocalDate.of(2000, 1, 1)),
                Description.EMPTY);

        assertProjectParseSuccess(
                " n/CS2103T d/2000-01-01",
                expectedProject);
    }

    @Test
    public void parse_validLeapDay_success() {
        Project expectedProject = new Project(
                new ProjectName("CS2103T"),
                new Deadline(LocalDate.of(2028, 2, 29)),
                Description.EMPTY);

        assertProjectParseSuccess(
                " n/CS2103T d/2028-02-29",
                expectedProject);
    }

    @Test
    public void parse_requiredPrefixMissing_failure() {
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, AddProjectCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " d/2026-11-13", expectedMessage);
        assertParseFailure(parser, " n/CS2103T", expectedMessage);
        assertParseFailure(parser, " desc/Build a contact manager", expectedMessage);
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser,
                " unexpected n/CS2103T d/2026-11-13",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddProjectCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_emptyValues_failure() {
        assertParseFailure(parser,
                " n/ d/2026-11-13",
                "PROJECT_NAME cannot be empty.");

        assertParseFailure(parser,
                " n/CS2103T d/ ",
                "DEADLINE cannot be empty.");

        assertParseFailure(parser,
                " n/CS2103T d/2026-11-13 desc/ ",
                ParserUtil.MESSAGE_EMPTY_DESCRIPTION);
    }

    @Test
    public void parse_duplicateNamePrefix_failure() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME);

        assertParseFailure(parser,
                " n/CS2103T n/Orbital d/2026-11-13",
                expectedMessage);

        assertParseFailure(parser,
                " n/CS2103T n/CS2103T d/2026-11-13",
                expectedMessage);
    }

    @Test
    public void parse_duplicateDeadlinePrefix_failure() {
        assertParseFailure(parser,
                " n/CS2103T d/2026-11-13 d/2026-11-14",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEADLINE));
    }

    @Test
    public void parse_duplicateDescriptionPrefix_failure() {
        assertParseFailure(parser,
                " n/CS2103T d/2026-11-13 desc/First desc/Second",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DESCRIPTION));
    }

    @Test
    public void parse_invalidProjectName_failure() {
        assertParseFailure(parser,
                " n/CS2103T@ d/2026-11-13",
                ProjectName.MESSAGE_CONSTRAINTS);

        assertParseFailure(parser,
                " n/--- d/2026-11-13",
                ProjectName.MESSAGE_CONSTRAINTS);

        assertParseFailure(parser,
                " n/" + "a".repeat(51) + " d/2026-11-13",
                ProjectName.MESSAGE_CONSTRAINTS);
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
            assertParseFailure(parser,
                    " n/CS2103T d/" + deadline,
                    ParserUtil.MESSAGE_INVALID_DEADLINE);
        }
    }

    @Test
    public void parse_descriptionAtLengthLimit_success() {
        String description = "a".repeat(200);
        Project expectedProject = new Project(
                new ProjectName("CS2103T"),
                new Deadline(LocalDate.of(2026, 11, 13)),
                new Description(description));

        assertProjectParseSuccess(
                " n/CS2103T d/2026-11-13 desc/" + description,
                expectedProject);
    }

    @Test
    public void parse_descriptionTooLong_failure() {
        assertParseFailure(parser,
                " n/CS2103T d/2026-11-13 desc/" + "a".repeat(201),
                Description.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_lineBreaks_failure() {
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, AddProjectCommand.MESSAGE_USAGE);
        String[] lineBreaks = {"\r", "\n", "\u0085", "\u2028", "\u2029"};

        for (String lineBreak : lineBreaks) {
            assertParseFailure(parser,
                    " n/CS2103T d/2026-11-13 desc/First" + lineBreak + "Second",
                    expectedMessage);

            // Trailing line breaks must be rejected before tokenisation trims them.
            assertParseFailure(parser,
                    " n/CS2103T d/2026-11-13 desc/Description" + lineBreak,
                    expectedMessage);
        }
    }

    /**
     * Checks that parsing and executing the input adds a project with the expected details.
     * The generated identifier is checked separately from those details.
     */
    private void assertProjectParseSuccess(String input, Project expectedProject) {
        AddProjectCommand command = assertDoesNotThrow(() -> parser.parse(input));
        Model model = new ModelManager();

        assertDoesNotThrow(() -> command.execute(model));

        assertEquals(1, model.getProjectList().size());
        Project actualProject = model.getProjectList().get(0);

        assertNotNull(actualProject.getProjectId());
        assertEquals(expectedProject.getName().toString(), actualProject.getName().toString());
        assertEquals(expectedProject.getDeadline(), actualProject.getDeadline());
        assertEquals(expectedProject.getDescription(), actualProject.getDescription());
    }
}
