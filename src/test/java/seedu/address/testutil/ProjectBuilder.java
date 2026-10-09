package seedu.address.testutil;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectId;
import seedu.address.model.project.ProjectName;

/**
 * A utility class to help with building Project objects.
 */
public class ProjectBuilder {

    public static final String DEFAULT_NAME = "Test";
    public static final LocalDate DEFAULT_DEADLINE = LocalDate.now();
    public static final String DEFAULT_DESCRIPTION = "Test description";

    private ProjectId projectId;
    private ProjectName name;
    private Deadline deadline;
    private Description description;

    /**
     * Creates a {@code ProjectBuilder} with the default details.
     */
    public ProjectBuilder() {
        projectId = ProjectId.generate();
        name = new ProjectName(DEFAULT_NAME);
        deadline = new Deadline(DEFAULT_DEADLINE);
        description = new Description(DEFAULT_DESCRIPTION);
    }

    /**
     * Initializes the ProjectBuilder with the data of {@code projectToCopy}.
     */
    public ProjectBuilder(Project projectToCopy) {
        requireNonNull(projectToCopy);
        projectId = projectToCopy.getProjectId();
        name = projectToCopy.getName();
        deadline = projectToCopy.getDeadline();
        description = projectToCopy.getDescription();
    }

    /**
     * Sets the {@code ProjectId} of the {@code Project} that we are building.
     */
    public ProjectBuilder withProjectId(ProjectId projectId) {
        requireNonNull(projectId);
        this.projectId = projectId;
        return this;
    }

    /**
     * Sets the {@code Name} of the {@code Project} that we are building.
     */
    public ProjectBuilder withName(String name) {
        this.name = new ProjectName(name);
        return this;
    }

    /**
     * Sets the {@code Deadline} of the {@code Project} that we are building.
     */
    public ProjectBuilder withDeadline(LocalDate deadline) {
        this.deadline = new Deadline(deadline);
        return this;
    }

    /**
     * Sets the {@code Description} of the {@code Project} that we are building.
     */
    public ProjectBuilder withDescription(String description) {
        this.description = new Description(description);
        return this;
    }

    public Project build() {
        return new Project(name, deadline, description);
    }
}
