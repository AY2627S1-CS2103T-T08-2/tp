package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalProjects.PROJ0;
import static seedu.address.testutil.TypicalProjects.PROJ1;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.project.exceptions.DuplicateProjectException;
import seedu.address.model.project.exceptions.ProjectNotFoundException;

public class UniqueProjectListTest {

    private final UniqueProjectList uniqueProjectList = new UniqueProjectList();

    @Test
    public void contains_nullProject_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueProjectList.contains(null));
    }

    @Test
    public void contains_projectNotInList_returnsFalse() {
        assertFalse(uniqueProjectList.contains(PROJ0));
    }

    @Test
    public void contains_projectInList_returnsTrue() {
        uniqueProjectList.add(PROJ0);
        assertTrue(uniqueProjectList.contains(PROJ0));
    }

    @Test
    public void add_nullProject_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueProjectList.add(null));
    }

    @Test
    public void add_duplicateProject_throwsDuplicateProjectException() {
        uniqueProjectList.add(PROJ0);
        assertThrows(DuplicateProjectException.class, () -> uniqueProjectList.add(PROJ0));
    }

    @Test
    public void setProject_nullTargetProject_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueProjectList.setProject(null, PROJ0));
    }

    @Test
    public void setProject_nullEditedProject_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueProjectList.setProject(PROJ0, null));
    }

    @Test
    public void setProject_targetProjectNotInList_throwsProjectNotFoundException() {
        assertThrows(ProjectNotFoundException.class, () -> uniqueProjectList.setProject(PROJ0, PROJ0));
    }

    @Test
    public void setProject_editedProjectIsSameProject_success() {
        uniqueProjectList.add(PROJ0);
        uniqueProjectList.setProject(PROJ0, PROJ0);
        UniqueProjectList expectedUniqueProjectList = new UniqueProjectList();
        expectedUniqueProjectList.add(PROJ0);
        assertEquals(expectedUniqueProjectList, uniqueProjectList);
    }

    @Test
    public void setProject_editedProjectHasDifferentIdentity_success() {
        uniqueProjectList.add(PROJ0);
        uniqueProjectList.setProject(PROJ0, PROJ1);
        UniqueProjectList expectedUniqueProjectList = new UniqueProjectList();
        expectedUniqueProjectList.add(PROJ1);
        assertEquals(expectedUniqueProjectList, uniqueProjectList);
    }

    @Test
    public void setProject_editedProjectHasNonUniqueIdentity_throwsDuplicateProjectException() {
        uniqueProjectList.add(PROJ0);
        uniqueProjectList.add(PROJ1);
        assertThrows(DuplicateProjectException.class, () -> uniqueProjectList.setProject(PROJ0, PROJ1));
    }

    @Test
    public void remove_nullProject_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueProjectList.remove(null));
    }

    @Test
    public void remove_projectDoesNotExist_throwsProjectNotFoundException() {
        assertThrows(ProjectNotFoundException.class, () -> uniqueProjectList.remove(PROJ0));
    }

    @Test
    public void remove_existingProject_removesProject() {
        uniqueProjectList.add(PROJ0);
        uniqueProjectList.remove(PROJ0);
        UniqueProjectList expectedUniqueProjectList = new UniqueProjectList();
        assertEquals(expectedUniqueProjectList, uniqueProjectList);
    }

    @Test
    public void setProjects_nullUniqueProjectList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueProjectList.setProjects((UniqueProjectList) null));
    }

    @Test
    public void setProjects_uniqueProjectList_replacesOwnListWithProvidedUniqueProjectList() {
        uniqueProjectList.add(PROJ0);
        UniqueProjectList expectedUniqueProjectList = new UniqueProjectList();
        expectedUniqueProjectList.add(PROJ1);
        uniqueProjectList.setProjects(expectedUniqueProjectList);
        assertEquals(expectedUniqueProjectList, uniqueProjectList);
    }

    @Test
    public void setProjects_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueProjectList.setProjects((List<Project>) null));
    }

    @Test
    public void setProjects_list_replacesOwnListWithProvidedList() {
        uniqueProjectList.add(PROJ0);
        List<Project> projectList = List.of(PROJ1);
        uniqueProjectList.setProjects(projectList);
        UniqueProjectList expectedUniqueProjectList = new UniqueProjectList();
        expectedUniqueProjectList.add(PROJ1);
        assertEquals(expectedUniqueProjectList, uniqueProjectList);
    }

    @Test
    public void setProjects_listWithDuplicateProjects_throwsDuplicateProjectException() {
        List<Project> listWithDuplicateProjects = List.of(PROJ0, PROJ0);
        assertThrows(DuplicateProjectException.class, () -> uniqueProjectList.setProjects(listWithDuplicateProjects));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, ()
                -> uniqueProjectList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniqueProjectList.asUnmodifiableObservableList().toString(), uniqueProjectList.toString());
    }

    @Test
    public void add_sameNameDifferentData_throwsDuplicateProjectException() {
        Project original = new Project(
                new ProjectName("CS2103T Team"),
                new Deadline(LocalDate.of(2026, 11, 1)),
                new Description("Original"));
        Project duplicate = new Project(
                new ProjectName("CS2103T Team"),
                new Deadline(LocalDate.of(2026, 12, 1)),
                new Description("Updated"));
        uniqueProjectList.add(original);

        assertThrows(DuplicateProjectException.class, () -> uniqueProjectList.add(duplicate));
        assertEquals(List.of(original), uniqueProjectList.asUnmodifiableObservableList());
    }

    @Test
    public void add_nameWithDifferentCaseAndSpacing_throwsDuplicateProjectException() {
        Deadline deadline = new Deadline(LocalDate.of(2026, 11, 1));
        Project original = new Project(
                new ProjectName("CS2103T Team"), deadline, Description.EMPTY);
        Project duplicate = new Project(
                new ProjectName("  cs2103t   team  "), deadline, Description.EMPTY);
        uniqueProjectList.add(original);

        assertThrows(DuplicateProjectException.class, () -> uniqueProjectList.add(duplicate));
        assertEquals(List.of(original), uniqueProjectList.asUnmodifiableObservableList());
    }

    @Test
    public void setProject_sameNameDifferentData_success() {
        ProjectName name = new ProjectName("CS2103T");
        Project original = new Project(name,
                new Deadline(LocalDate.of(2026, 11, 1)),
                new Description("Original"));
        Project updated = new Project(name,
                new Deadline(LocalDate.of(2026, 12, 1)),
                new Description("Updated"));
        uniqueProjectList.add(original);

        uniqueProjectList.setProject(original, updated);

        assertEquals(List.of(updated), uniqueProjectList.asUnmodifiableObservableList());
    }
}
