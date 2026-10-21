package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectId;
import seedu.address.model.project.ProjectName;

/**
 * Jackson-friendly version of {@link Project}.
 */
class JsonAdaptedProject {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Project's %s field is missing!";
    public static final String INVALID_DEADLINE_MESSAGE = "Project deadlines must use the YYYY-MM-DD format.";

    private final String id;
    private final String name;
    private final String deadline;
    private final String description;

    /**
     * Constructs a {@code JsonAdaptedProject} with the given project details.
     */
    @JsonCreator
    public JsonAdaptedProject(@JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("deadline") String deadline,
            @JsonProperty("description") String description) {
        this.id = id;
        this.name = name;
        this.deadline = deadline;
        this.description = description;
    }

    /**
     * Converts a given {@code Project} into this class for Jackson use.
     */
    public JsonAdaptedProject(Project source) {
        id = source.getProjectId().toString();
        name = source.getName().toString();
        deadline = source.getDeadline().toString();
        description = source.getDescription().toString();
    }

    /**
     * Converts this Jackson-friendly project into the model's {@code Project} object.
     *
     * @throws IllegalValueException if any stored field is missing or invalid.
     */
    public Project toModelType() throws IllegalValueException {
        if (id == null) {
            throw new IllegalValueException(String.format(
                    MISSING_FIELD_MESSAGE_FORMAT, ProjectId.class.getSimpleName()));
        }
        final ProjectId modelProjectId;
        try {
            modelProjectId = ProjectId.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(ProjectId.MESSAGE_CONSTRAINTS, e);
        }

        if (name == null) {
            throw new IllegalValueException(String.format(
                    MISSING_FIELD_MESSAGE_FORMAT, ProjectName.class.getSimpleName()));
        }
        if (!ProjectName.isValidName(name)) {
            throw new IllegalValueException(ProjectName.MESSAGE_CONSTRAINTS);
        }
        final ProjectName modelName = new ProjectName(name);

        if (deadline == null) {
            throw new IllegalValueException(String.format(
                    MISSING_FIELD_MESSAGE_FORMAT, Deadline.class.getSimpleName()));
        }
        final Deadline modelDeadline;
        try {
            modelDeadline = new Deadline(LocalDate.parse(deadline));
        } catch (DateTimeParseException e) {
            throw new IllegalValueException(INVALID_DEADLINE_MESSAGE, e);
        }

        if (description == null) {
            throw new IllegalValueException(String.format(
                    MISSING_FIELD_MESSAGE_FORMAT, Description.class.getSimpleName()));
        }
        if (!Description.isValidDescription(description)) {
            throw new IllegalValueException(Description.MESSAGE_CONSTRAINTS);
        }
        final Description modelDescription = new Description(description);

        return new Project(modelProjectId, modelName, modelDeadline, modelDescription);
    }
}
