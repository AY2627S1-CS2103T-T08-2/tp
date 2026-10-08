package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.model.project.Project;

/**
 * Jackson representation of a project.
 */
public class JsonAdaptedProject {
    private final int id;
    private final String name;

    /**
     * Creates an adapted project from its JSON fields.
     *
     * @param id numeric project identifier
     * @param name project name
     */
    @JsonCreator
    public JsonAdaptedProject(@JsonProperty("id") int id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    /** Creates an adapted project from its model representation. */
    public JsonAdaptedProject(Project source) {
        this(source.getId(), source.getName());
    }

    /** Returns the project identifier in the JSON representation. */
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    /** Returns the project name in the JSON representation. */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Converts this adapted project to its model representation.
     *
     * @return the corresponding project
     */
    public Project toModelType() {
        return new Project(id, name);
    }
}
