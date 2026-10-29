package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectName;
import seedu.address.testutil.PersonBuilder;

public class AddCommandTest {

    @Test
    public void execute_sameNameWithDifferentDetails_success() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        Model model = new ModelManager(addressBook, new UserPrefs());
        Person otherAlice = new PersonBuilder(ALICE)
            .withPersonId(PersonId.generate())
            .withPhone("91234567")
            .build();
        AddressBook expectedAddressBook = new AddressBook(addressBook);
        expectedAddressBook.addPerson(otherAlice);
        Model expectedModel = new ModelManager(expectedAddressBook, new UserPrefs());

        assertCommandSuccess(new AddCommand(otherAlice), model,
            String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(otherAlice)), expectedModel);
        assertEquals(List.of(ALICE.getPersonId(), otherAlice.getPersonId()),
            model
                .getAddressBook()
                .getPersonList()
                .stream()
                .map(Person::getPersonId)
                .toList());
    }

    @Test
    public void execute_matchingDetailsWithDifferentId_failure() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        Model model = new ModelManager(addressBook, new UserPrefs());
        Person duplicate = new PersonBuilder(ALICE)
            .withPersonId(PersonId.generate())
            .build();

        assertCommandFailure(new AddCommand(duplicate), model, AddCommand.MESSAGE_DUPLICATE_PERSON);
        assertEquals(List.of(ALICE.getPersonId()),
            model
                .getAddressBook()
                .getPersonList()
                .stream()
                .map(Person::getPersonId)
                .toList());
    }

    @Test
    public void execute_duplicateHiddenByFilter_failure() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BOB);
        Model model = new ModelManager(addressBook, new UserPrefs());
        model.updateFilteredPersonList(person -> person
            .getPersonId()
            .equals(BOB.getPersonId()));
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        Person duplicate = new PersonBuilder(ALICE)
            .withPersonId(PersonId.generate())
            .build();

        assertCommandFailure(new AddCommand(duplicate), model, AddCommand.MESSAGE_DUPLICATE_PERSON);
        assertEquals(List.of(ALICE.getPersonId(), BOB.getPersonId()),
            model
                .getAddressBook()
                .getPersonList()
                .stream()
                .map(Person::getPersonId)
                .toList());
    }

    @Test
    public void constructor_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddCommand(null));
    }

    @Test
    public void execute_personAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person validPerson = new PersonBuilder().build();

        CommandResult commandResult = new AddCommand(validPerson).execute(modelStub);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
            commandResult.getFeedbackToUser());
        assertEquals(List.of(validPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person validPerson = new PersonBuilder().build();
        AddCommand addCommand = new AddCommand(validPerson);
        ModelStub modelStub = new ModelStubWithPerson(validPerson);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> addCommand.execute(modelStub));
    }

    @Test
    public void equals() {
        Person alice = new PersonBuilder()
            .withName("Alice")
            .build();
        Person bob = new PersonBuilder()
            .withName("Bob")
            .build();
        AddCommand addAliceCommand = new AddCommand(alice);
        AddCommand addBobCommand = new AddCommand(bob);

        // same object -> returns true
        assertTrue(addAliceCommand.equals(addAliceCommand));

        // same values -> returns true
        AddCommand addAliceCommandCopy = new AddCommand(alice);
        assertTrue(addAliceCommand.equals(addAliceCommandCopy));

        // different types -> returns false
        assertFalse(addAliceCommand.equals(1));

        // null -> returns false
        assertFalse(addAliceCommand.equals(null));

        // different person -> returns false
        assertFalse(addAliceCommand.equals(addBobCommand));
    }

    @Test
    public void toStringMethod() {
        AddCommand addCommand = new AddCommand(ALICE);
        String expected = AddCommand.class.getCanonicalName() + "{toAdd=" + ALICE + "}";
        assertEquals(expected, addCommand.toString());
    }

    /**
     * A default model stub that has all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public GuiSettings getGuiSettings() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setAddressBook(ReadOnlyAddressBook newData) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deletePerson(Person target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasProject(Project project) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addProject(Project project) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setProject(Project target, Project editedProject) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deleteProject(Project target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Project> getFilteredProjectList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public Optional<Project> findProject(ProjectName name) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public List<Person> getProjectMembers(Project project) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public List<Project> getPersonProjects(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasProjectMembership(ProjectMembership membership) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addProjectMembership(ProjectMembership membership) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setProjectMembership(ProjectMembership membership) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void removeProjectMembership(ProjectMembership membership) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public Predicate<? super Person> getFilteredPersonListPredicate() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public Predicate<? super Project> getFilteredProjectListPredicate() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredProjectList(Predicate<Project> predicate) {
            throw new AssertionError("This method should not be called.");
        }
    }

    /**
     * A Model stub that contains a single person.
     */
    private class ModelStubWithPerson extends ModelStub {
        private final Person person;

        ModelStubWithPerson(Person person) {
            requireNonNull(person);
            this.person = person;
        }

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return this.person.isSamePerson(person);
        }
    }

    /**
     * A Model stub that always accepts the person being added.
     */
    private class ModelStubAcceptingPersonAdded extends ModelStub {
        final ArrayList<Person> personsAdded = new ArrayList<>();

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return personsAdded
                .stream()
                .anyMatch(person::isSamePerson);
        }

        @Override
        public void addPerson(Person person) {
            requireNonNull(person);
            personsAdded.add(person);
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }

}
