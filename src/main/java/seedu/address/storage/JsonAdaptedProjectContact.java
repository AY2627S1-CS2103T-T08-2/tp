package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.model.project.ProjectContact;

/**
 * Jackson representation of a project-to-contact association.
 */
public class JsonAdaptedProjectContact {
    private final int projectId;
    private final String contactName;

    /**
     * Creates an adapted association from its JSON fields.
     *
     * @param projectId identifier of the associated project
     * @param contactName unique name of the associated contact
     */
    @JsonCreator
    public JsonAdaptedProjectContact(@JsonProperty("projectId") int projectId,
            @JsonProperty("contactName") String contactName) {
        this.projectId = projectId;
        this.contactName = contactName;
    }

    /** Creates an adapted association from its model representation. */
    public JsonAdaptedProjectContact(ProjectContact source) {
        this(source.getProjectId(), source.getContactName());
    }

    /** Returns the project identifier in the JSON representation. */
    @JsonProperty("projectId")
    public int getProjectId() {
        return projectId;
    }

    /** Returns the contact name in the JSON representation. */
    @JsonProperty("contactName")
    public String getContactName() {
        return contactName;
    }

    /**
     * Converts this adapted association to its model representation.
     *
     * @return the corresponding project-contact association
     */
    public ProjectContact toModelType() {
        return new ProjectContact(projectId, contactName);
    }
}
