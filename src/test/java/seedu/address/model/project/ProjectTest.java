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

        assertEquals(PROJ0.hashCode(), proj0Copy.hashCode());
    }

    @Test
    public void constructor_nullFields_throwsNullPointerException() {
        ProjectId id = PROJ0.getProjectId();
        ProjectName name = new ProjectName("CS2103T");
        Deadline deadline = new Deadline(LocalDate.of(2026, 11, 1));
        Description description = new Description("Team project");

        assertThrows(NullPointerException.class, () -> new Project(null, deadline, description));
        assertThrows(NullPointerException.class, () -> new Project(name, null, description));
        assertThrows(NullPointerException.class, () -> new Project(name, deadline, null));
        assertThrows(NullPointerException.class, () -> new Project(null, name, deadline, description));
        assertThrows(NullPointerException.class, () -> new Project(id, null, deadline, description));
        assertThrows(NullPointerException.class, () -> new Project(id, name, null, description));
        assertThrows(NullPointerException.class, () -> new Project(id, name, deadline, null));
    }

    @Test
    public void getters_returnsExpectedValues() {
        ProjectId id = PROJ0.getProjectId();
        ProjectName name = new ProjectName("CS2103T");
        Deadline deadline = new Deadline(LocalDate.of(2026, 11, 1));
        Description description = new Description("Team project");
        Project project = new Project(id, name, deadline, description);

        assertEquals(id, project.getProjectId());
        assertEquals(name, project.getName());
        assertEquals(deadline, project.getDeadline());
        assertEquals(description, project.getDescription());
    }

    @Test
    public void isSameProject_sameIdDifferentData_returnsTrue() {
        Project updated = new ProjectBuilder(PROJ0)
                .withName("Renamed Project")
                .withDeadline(LocalDate.of(2027, 1, 1))
                .withDescription("Updated description")
                .build();

        assertTrue(PROJ0.isSameProject(updated));
        assertTrue(updated.isSameProject(PROJ0));
    }

    @Test
    public void isSameProject_differentIdSameData_returnsFalse() {
        Project other = new ProjectBuilder(PROJ0)
                .withProjectId(PROJ1.getProjectId())
                .build();

        assertFalse(PROJ0.isSameProject(other));
    }

    @Test
    public void equals_onlyDeadlineDiffers_returnsFalse() {
        Project updated = new ProjectBuilder(PROJ0)
                .withDeadline(PROJ0.getDeadline().value.plusDays(1))
                .build();

        assertFalse(PROJ0.equals(updated));
    }

    @Test
    public void equals_onlyDescriptionDiffers_returnsFalse() {
        Project updated = new ProjectBuilder(PROJ0)
                .withDescription("Updated description")
                .build();

        assertFalse(PROJ0.equals(updated));
    }

    @Test
    public void equals_onlyIdDiffers_returnsFalse() {
        Project other = new ProjectBuilder(PROJ0)
                .withProjectId(PROJ1.getProjectId())
                .build();

        assertFalse(PROJ0.equals(other));
    }

    @Test
    public void equals_onlyNameDiffers_returnsFalse() {
        Project renamed = new ProjectBuilder(PROJ0)
                .withName("Renamed Project")
                .build();

        assertFalse(PROJ0.equals(renamed));
    }
}
