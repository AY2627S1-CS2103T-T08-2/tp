package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedProject.INVALID_DEADLINE_MESSAGE;
import static seedu.address.storage.JsonAdaptedProject.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalProjects.PROJ1;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.ProjectId;
import seedu.address.model.project.ProjectName;

public class JsonAdaptedProjectTest {

    private static final String INVALID_ID = "not-a-uuid";
    private static final String INVALID_NAME = "Invalid @ Project";
    private static final String INVALID_DEADLINE = "2026-02-30";
    private static final String INVALID_DESCRIPTION = "First line\nSecond line";

    private static final String VALID_ID = PROJ1.getProjectId().toString();
    private static final String VALID_NAME = PROJ1.getName().toString();
    private static final String VALID_DEADLINE = PROJ1.getDeadline().toString();
    private static final String VALID_DESCRIPTION = PROJ1.getDescription().toString();

    @Test
    public void toModelType_validProjectDetails_returnsProject() throws Exception {
        JsonAdaptedProject project = new JsonAdaptedProject(PROJ1);

        assertEquals(PROJ1, project.toModelType());
    }

    @Test
    public void jsonRoundTrip_validProject_preservesIdAndDetails() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedProject(PROJ1));
        JsonAdaptedProject project = JsonUtil.fromJsonString(json, JsonAdaptedProject.class);

        assertEquals(PROJ1, project.toModelType());
    }

    @Test
    public void toModelType_invalidId_throwsIllegalValueException() {
        JsonAdaptedProject project = new JsonAdaptedProject(
                INVALID_ID, VALID_NAME, VALID_DEADLINE, VALID_DESCRIPTION);

        assertThrows(IllegalValueException.class, ProjectId.MESSAGE_CONSTRAINTS, project::toModelType);
    }

    @Test
    public void toModelType_nullId_throwsIllegalValueException() {
        JsonAdaptedProject project = new JsonAdaptedProject(
                null, VALID_NAME, VALID_DEADLINE, VALID_DESCRIPTION);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, ProjectId.class.getSimpleName());

        assertThrows(IllegalValueException.class, expectedMessage, project::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedProject project = new JsonAdaptedProject(
                VALID_ID, INVALID_NAME, VALID_DEADLINE, VALID_DESCRIPTION);

        assertThrows(IllegalValueException.class, ProjectName.MESSAGE_CONSTRAINTS, project::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedProject project = new JsonAdaptedProject(
                VALID_ID, null, VALID_DEADLINE, VALID_DESCRIPTION);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, ProjectName.class.getSimpleName());

        assertThrows(IllegalValueException.class, expectedMessage, project::toModelType);
    }

    @Test
    public void toModelType_invalidDeadline_throwsIllegalValueException() {
        JsonAdaptedProject project = new JsonAdaptedProject(
                VALID_ID, VALID_NAME, INVALID_DEADLINE, VALID_DESCRIPTION);

        assertThrows(IllegalValueException.class, INVALID_DEADLINE_MESSAGE, project::toModelType);
    }

    @Test
    public void toModelType_nullDeadline_throwsIllegalValueException() {
        JsonAdaptedProject project = new JsonAdaptedProject(
                VALID_ID, VALID_NAME, null, VALID_DESCRIPTION);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Deadline.class.getSimpleName());

        assertThrows(IllegalValueException.class, expectedMessage, project::toModelType);
    }

    @Test
    public void toModelType_invalidDescription_throwsIllegalValueException() {
        JsonAdaptedProject project = new JsonAdaptedProject(
                VALID_ID, VALID_NAME, VALID_DEADLINE, INVALID_DESCRIPTION);

        assertThrows(IllegalValueException.class, Description.MESSAGE_CONSTRAINTS, project::toModelType);
    }

    @Test
    public void toModelType_nullDescription_throwsIllegalValueException() {
        JsonAdaptedProject project = new JsonAdaptedProject(
                VALID_ID, VALID_NAME, VALID_DEADLINE, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Description.class.getSimpleName());

        assertThrows(IllegalValueException.class, expectedMessage, project::toModelType);
    }
}
