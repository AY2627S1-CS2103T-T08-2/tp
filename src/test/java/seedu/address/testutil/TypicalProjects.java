package seedu.address.testutil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.project.Project;

/**
 * A utility class containing a list of {@code Project} objects to be used in tests.
 */
public class TypicalProjects {

    public static final Project PROJ0 = new ProjectBuilder().withName("Project 0")
            .withDeadline(LocalDate.now())
            .withDescription("This is Project 0").build();

    public static final Project PROJ1 = new ProjectBuilder().withName("Project 1")
            .withDeadline(LocalDate.now())
            .withDescription("This is Project 1").build();

    public static final Project PROJ2 = new ProjectBuilder().withName("Project 2")
            .withDeadline(LocalDate.now())
            .withDescription("This is Project 2").build();

    public static final Project PROJ3 = new ProjectBuilder().withName("Project 3")
            .withDeadline(LocalDate.now())
            .withDescription("This is Project 3").build();

    public static final Project PROJ4 = new ProjectBuilder().withName("Project 4")
            .withDeadline(LocalDate.now())
            .withDescription("This is Project 4").build();

    private TypicalProjects() {} // prevents instantiation

    /**
     * Returns an {@code AddressBook} with all the typical projects.
     */
    public static AddressBook getTypicalAddressBook() {
        AddressBook ab = new AddressBook();
        for (Project project : getTypicalProjects()) {
            ab.addProject(project);
        }
        return ab;
    }

    public static List<Project> getTypicalProjects() {
        return new ArrayList<>(Arrays.asList(PROJ0, PROJ1, PROJ2, PROJ3, PROJ4));
    }
}
