package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalProjects.PROJ0;
import static seedu.address.testutil.TypicalProjects.PROJ1;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ProjectBuilder;

public class ProjectTest {

    @Test
    public void isSameProject() {
        // same object -> returns true
        assertTrue(PROJ0.isSameProject(PROJ0));

        // null -> returns false
        assertFalse(PROJ0.isSameProject(null));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Project proj0Copy = new ProjectBuilder(PROJ0).build();
        assertTrue(PROJ0.equals(proj0Copy));

        // same object -> returns true
        assertTrue(PROJ0.equals(PROJ0));

        // null -> returns false
        assertFalse(PROJ0.equals(null));

        // different type -> returns false
        assertFalse(PROJ0.equals(5));

        // different Project -> returns false
        assertFalse(PROJ0.equals(PROJ1));
    }

    @Test
    public void constructor_nullFields_throwsNullPointerException() {
        ProjectName name = new ProjectName("CS2103T");
        Deadline deadline = new Deadline(LocalDate.of(2026, 11, 1));
        Description description = new Description("Team project");

        assertThrows(NullPointerException.class, () -> new Project(null, deadline, description));
        assertThrows(NullPointerException.class, () -> new Project(name, null, description));
        assertThrows(NullPointerException.class, () -> new Project(name, deadline, null));
    }

    @Test
    public void getters_returnsExpectedValues() {
        ProjectName name = new ProjectName("CS2103T");
        Deadline deadline = new Deadline(LocalDate.of(2026, 11, 1));
        Description description = new Description("Team project");
        Project project = new Project(name, deadline, description);

        assertEquals(name, project.getName());
        assertEquals(deadline, project.getDeadline());
        assertEquals(description, project.getDescription());
    }

    @Test
    public void isSameProject_sameNameDifferentData_returnsTrue() {
        Project original = new Project(
                new ProjectName("CS2103T"),
                new Deadline(LocalDate.of(2026, 11, 1)),
                new Description("Original description"));
        Project updated = new Project(
                new ProjectName("  cs2103t  "),
                new Deadline(LocalDate.of(2026, 12, 1)),
                new Description("Updated description"));

        assertTrue(original.isSameProject(updated));
        assertTrue(updated.isSameProject(original));
    }

    @Test
    public void isSameProject_differentName_returnsFalse() {
        Deadline deadline = new Deadline(LocalDate.of(2026, 11, 1));
        Project first = new Project(new ProjectName("CS2103T"), deadline, Description.EMPTY);
        Project second = new Project(new ProjectName("Orbital"), deadline, Description.EMPTY);

        assertFalse(first.isSameProject(second));
    }

    @Test
    public void equals_onlyDeadlineDiffers_returnsFalse() {
        ProjectName name = new ProjectName("CS2103T");
        Project first = new Project(name,
                new Deadline(LocalDate.of(2026, 11, 1)), Description.EMPTY);
        Project second = new Project(name,
                new Deadline(LocalDate.of(2026, 12, 1)), Description.EMPTY);

        assertFalse(first.equals(second));
    }

    @Test
    public void equals_onlyDescriptionDiffers_returnsFalse() {
        ProjectName name = new ProjectName("CS2103T");
        Deadline deadline = new Deadline(LocalDate.of(2026, 11, 1));
        Project first = new Project(name, deadline, new Description("Original"));
        Project second = new Project(name, deadline, new Description("Updated"));

        assertFalse(first.equals(second));
    }
}
