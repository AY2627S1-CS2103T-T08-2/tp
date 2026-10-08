package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;
import seedu.address.model.project.exceptions.DuplicateProjectException;
import seedu.address.testutil.AddressBookBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void addProject_newProject_updatesAddressBookAndList() {
        ModelManager model = new ModelManager();
        Project project = createProject("CS2103T");

        // Obtain the view before adding to verify that it stays up to date.
        var projectList = model.getProjectList();

        assertFalse(model.hasProject(project));

        model.addProject(project);

        assertTrue(model.hasProject(project));
        assertEquals(List.of(project), projectList);
        assertEquals(List.of(project), model.getAddressBook().getProjectList());
    }

    @Test
    public void addProject_duplicateName_throwsDuplicateProjectException() {
        ModelManager model = new ModelManager();
        Project original = createProject("CS2103T Team");
        Project duplicate = createProject("  cs2103t   team  ");
        model.addProject(original);

        assertThrows(DuplicateProjectException.class, () -> model.addProject(duplicate));

        assertEquals(List.of(original), model.getProjectList());
    }

    @Test
    public void findProject_matchingName_returnsProject() {
        ModelManager model = new ModelManager();
        Project project = createProject("CS2103T Team");
        model.addProject(project);

        assertEquals(Optional.of(project),
                model.findProject(new ProjectName("  cs2103t   team  ")));
    }

    @Test
    public void findProject_missingOrPartialName_returnsEmpty() {
        ModelManager model = new ModelManager();
        model.addProject(createProject("CS2103T Team"));

        assertEquals(Optional.empty(),
                model.findProject(new ProjectName("Orbital")));
        assertEquals(Optional.empty(),
                model.findProject(new ProjectName("CS2103T")));
    }

    @Test
    public void findProject_nullName_throwsNullPointerException() {
        ModelManager model = new ModelManager();

        assertThrows(NullPointerException.class, () -> model.findProject(null));
    }

    @Test
    public void constructor_withProjects_preservesProjects() {
        AddressBook addressBook = new AddressBook();
        Project project = createProject("CS2103T");
        addressBook.addProject(project);

        ModelManager model = new ModelManager(addressBook, new UserPrefs());

        assertEquals(List.of(project), model.getProjectList());
        assertEquals(Optional.of(project), model.findProject(project.getName()));
    }

    @Test
    public void setAddressBook_withProjects_replacesProjects() {
        ModelManager model = new ModelManager();
        model.addProject(createProject("Old Project"));
        var projectList = model.getProjectList();

        AddressBook replacement = new AddressBook();
        Project project = createProject("New Project");
        replacement.addProject(project);

        model.setAddressBook(replacement);

        assertEquals(List.of(project), projectList);
        assertEquals(Optional.of(project), model.findProject(project.getName()));
        assertEquals(Optional.empty(),
                model.findProject(new ProjectName("Old Project")));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    private Project createProject(String name) {
        return new Project(
                new ProjectName(name),
                new Deadline(LocalDate.of(2026, 11, 1)),
                Description.EMPTY);
    }
}
