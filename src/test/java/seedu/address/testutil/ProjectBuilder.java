package seedu.address.testutil;

import java.time.LocalDate;
import java.util.HashSet;

import seedu.address.model.person.Person;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.ProjectName;

/**
 * A utility class to help with building Project objects.
 */
public class ProjectBuilder {

    public static final String DEFAULT_NAME = "Test";
    public static final LocalDate DEFAULT_DEADLINE = LocalDate.now();
    public static final String DEFAULT_DESCRIPTION = "Test description";

    private ProjectName name;
    private Deadline deadline;
    private Description description;

    /**
     * Creates a {@code ProjectBuilder} with the default details.
     */
    public ProjectBuilder() {
        name = new ProjectName(DEFAULT_NAME);
        deadline = new Deadline(DEFAULT_DEADLINE);
        description = new Description(DEFAULT_DESCRIPTION);
    }

    /**
     * Initializes the ProjectBuilder with the data of {@code projectToCopy}.
     */
    public ProjectBuilder(Person projectToCopy) {
        name = projectToCopy.getName();

    }
}
