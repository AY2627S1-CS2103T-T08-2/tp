package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.ProjectId;

/**
 * Jackson-friendly version of {@link ProjectMembership}.
 */
class JsonAdaptedProjectMembership {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Project membership's %s field is missing!";

    private final String personId;
    private final String projectId;

    /**
     * Constructs a {@code JsonAdaptedProjectMembership} using the stored identifiers.
     */
    @JsonCreator
    public JsonAdaptedProjectMembership(@JsonProperty("personId") String personId,
            @JsonProperty("projectId") String projectId) {
        this.personId = personId;
        this.projectId = projectId;
    }

    /**
     * Converts a given {@code ProjectMembership} into this class for Jackson use.
     */
    public JsonAdaptedProjectMembership(ProjectMembership source) {
        personId = source.getPersonId().toString();
        projectId = source.getProjectId().toString();
    }

    /**
     * Converts this Jackson-friendly membership into the model's {@code ProjectMembership} object.
     *
     * @throws IllegalValueException if either stored identifier is missing or invalid.
     */
    public ProjectMembership toModelType() throws IllegalValueException {
        if (personId == null) {
            throw new IllegalValueException(String.format(
                    MISSING_FIELD_MESSAGE_FORMAT, PersonId.class.getSimpleName()));
        }
        final PersonId modelPersonId;
        try {
            modelPersonId = PersonId.fromString(personId);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(PersonId.MESSAGE_CONSTRAINTS, e);
        }

        if (projectId == null) {
            throw new IllegalValueException(String.format(
                    MISSING_FIELD_MESSAGE_FORMAT, ProjectId.class.getSimpleName()));
        }
        final ProjectId modelProjectId;
        try {
            modelProjectId = ProjectId.fromString(projectId);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(ProjectId.MESSAGE_CONSTRAINTS, e);
        }

        return new ProjectMembership(modelPersonId, modelProjectId);
    }
}
