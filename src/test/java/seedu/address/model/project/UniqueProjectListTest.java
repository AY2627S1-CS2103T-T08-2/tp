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
import seedu.address.testutil.ProjectBuilder;

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
    public void setProject_changedId_throwsIllegalArgumentException() {
        uniqueProjectList.add(PROJ0);
        Project changedId = new ProjectBuilder(PROJ0)
                .withProjectId(PROJ1.getProjectId())
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> uniqueProjectList.setProject(PROJ0, changedId));

        assertEquals(List.of(PROJ0), uniqueProjectList.asUnmodifiableObservableList());
    }

    @Test
    public void setProject_existingName_throwsDuplicateProjectException() {
        uniqueProjectList.add(PROJ0);
        uniqueProjectList.add(PROJ1);

        Project renamed = new ProjectBuilder(PROJ0)
                .withName(PROJ1.getName().toString())
                .build();

        assertThrows(DuplicateProjectException.class,
                () -> uniqueProjectList.setProject(PROJ0, renamed));

        assertEquals(List.of(PROJ0, PROJ1), uniqueProjectList.asUnmodifiableObservableList());
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
    public void setProject_sameIdDifferentData_success() {
        uniqueProjectList.add(PROJ0);

        Project edited = new ProjectBuilder(PROJ0)
                .withDescription("Updated description")
                .build();

        uniqueProjectList.setProject(PROJ0, edited);

        assertEquals(List.of(edited), uniqueProjectList.asUnmodifiableObservableList());
    }

    @Test
    public void setProject_sameIdNewName_success() {
        uniqueProjectList.add(PROJ0);
        Project renamed = new ProjectBuilder(PROJ0)
                .withName("Renamed Project")
                .build();

        uniqueProjectList.setProject(PROJ0, renamed);

        assertEquals(List.of(renamed), uniqueProjectList.asUnmodifiableObservableList());
    }

    @Test
    public void contains_sameIdDifferentDetails_returnsTrue() {
        uniqueProjectList.add(PROJ0);
        Project edited = new ProjectBuilder(PROJ0).withName("Different Name").build();

        assertTrue(uniqueProjectList.contains(edited));
    }

    @Test
    public void contains_differentIdSameDetails_returnsFalse() {
        uniqueProjectList.add(PROJ0);
        Project other = new ProjectBuilder(PROJ0)
                .withProjectId(PROJ1.getProjectId())
                .build();

        assertFalse(uniqueProjectList.contains(other));
    }

    @Test
    public void containsName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueProjectList.containsName(null));
    }

    @Test
    public void containsName_normalisedName_returnsTrue() {
        Project project = new ProjectBuilder().withName("CS2103T Team").build();
        uniqueProjectList.add(project);

        assertTrue(uniqueProjectList.containsName(new ProjectName("  cs2103t   team  ")));
    }

    @Test
    public void containsName_missingName_returnsFalse() {
        uniqueProjectList.add(PROJ0);

        assertFalse(uniqueProjectList.containsName(new ProjectName("Missing Project")));
    }

    @Test
    public void add_sameIdDifferentName_throwsDuplicateProjectException() {
        uniqueProjectList.add(PROJ0);
        Project duplicateId = new ProjectBuilder(PROJ0).withName("Different Name").build();

        assertThrows(DuplicateProjectException.class, () -> uniqueProjectList.add(duplicateId));

        assertEquals(List.of(PROJ0), uniqueProjectList.asUnmodifiableObservableList());
    }

    @Test
    public void setProjects_duplicateIds_throwsDuplicateProjectException() {
        uniqueProjectList.add(PROJ1);
        Project duplicateId = new ProjectBuilder(PROJ0).withName("Different Name").build();

        assertThrows(DuplicateProjectException.class,
                () -> uniqueProjectList.setProjects(List.of(PROJ0, duplicateId)));

        assertEquals(List.of(PROJ1), uniqueProjectList.asUnmodifiableObservableList());
    }

    @Test
    public void setProjects_duplicateNames_throwsDuplicateProjectException() {
        uniqueProjectList.add(PROJ1);
        Project duplicateName = new ProjectBuilder(PROJ0)
                .withProjectId(PROJ1.getProjectId())
                .build();

        assertThrows(DuplicateProjectException.class,
                () -> uniqueProjectList.setProjects(List.of(PROJ0, duplicateName)));

        assertEquals(List.of(PROJ1), uniqueProjectList.asUnmodifiableObservableList());
    }
}
