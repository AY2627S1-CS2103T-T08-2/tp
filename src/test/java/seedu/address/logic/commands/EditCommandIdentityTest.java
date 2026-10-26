package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.Project;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.ProjectBuilder;

/** Tests identifier preservation across edit-field, displayed-index, and failure partitions. */
public class EditCommandIdentityTest {
    private static final List<Person> ORIGINALS = List.of(ALICE,
            new PersonBuilder(BOB).withPersonId(new PersonId(new UUID(0, 0))).build(),
            new PersonBuilder().withName("Carol").withPersonId(new PersonId(new UUID(-1, -1))).build());

    @TestFactory
    public Stream<DynamicTest> execute_eachFieldAndFilterPartition_preservesContactIdentity() {
        return IntStream.range(0, ORIGINALS.size()).boxed().flatMap(position ->
                IntStream.range(0, 2).boxed().flatMap(filtered -> editCases(ORIGINALS.get(position)).stream()
                        .map(edit -> dynamicTest(edit.name() + " position " + position + " filter " + filtered, () -> {
                            Model model = createModel(ORIGINALS);
                            Person target = ORIGINALS.get(position);
                            Project project = new ProjectBuilder().build();
                            model.addProject(project);
                            ProjectMembership membership = new ProjectMembership(target.getPersonId(),
                                    project.getProjectId());
                            model.addProjectMembership(membership);
                            if (filtered == 1) {
                                model.updateFilteredPersonList(person ->
                                        person.getPersonId().equals(target.getPersonId()));
                            }
                            Index index = Index.fromOneBased(filtered == 1 ? 1 : position + 1);
                            List<Person> expected = new ArrayList<>(ORIGINALS);
                            expected.set(position, edit.expected());

                            CommandResult result = new EditCommand(index, edit.descriptor()).execute(model);

                            assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                                    Messages.format(edit.expected())), result.getFeedbackToUser());
                            assertStoredData(expected, model);
                            assertEquals(target.getPersonId(), model.getAddressBook().getPersonList()
                                    .get(position).getPersonId());
                            assertEquals(List.of(membership), model.getAddressBook().getProjectMembershipList());
                            assertEquals(List.of(edit.expected()), model.getProjectMembers(project));
                        }))));
    }

    @TestFactory
    public Stream<DynamicTest> execute_invalidIndexBoundaries_preservesEveryId() {
        return IntStream.of(0, 1, 3).boxed().flatMap(size ->
                IntStream.of(size + 1, Integer.MAX_VALUE).mapToObj(index ->
                        dynamicTest("list size " + size + " invalid index " + index, () -> {
                            List<Person> expected = ORIGINALS.subList(0, size);
                            Model model = createModel(expected);
                            EditCommand command = new EditCommand(Index.fromOneBased(index),
                                    new EditPersonDescriptorBuilder().withName("Edited Name").build());

                            assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                                    command.execute(model));

                            assertStoredData(expected, model);
                        })));
    }

    @Test
    public void execute_indexOutsideFilterButInsideFullList_preservesEveryIdAndFilter() {
        Model model = createModel(ORIGINALS);
        Person displayed = ORIGINALS.get(2);
        model.updateFilteredPersonList(person -> person.getPersonId().equals(displayed.getPersonId()));
        EditCommand command = new EditCommand(Index.fromOneBased(2),
                new EditPersonDescriptorBuilder().withName("Edited Name").build());

        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                command.execute(model));

        assertStoredData(ORIGINALS, model);
        assertEquals(List.of(displayed.getPersonId()),
                model.getFilteredPersonList().stream().map(Person::getPersonId).toList());
    }

    @TestFactory
    public Stream<DynamicTest> execute_duplicateDetailsWithDifferentId_preservesEveryId() {
        return IntStream.range(0, 2).mapToObj(filtered -> dynamicTest("duplicate edit filtered " + filtered, () -> {
            Model model = createModel(ORIGINALS);
            Person target = ORIGINALS.get(1);
            if (filtered == 1) {
                model.updateFilteredPersonList(person -> person.getPersonId().equals(target.getPersonId()));
            }
            EditCommand command = new EditCommand(Index.fromOneBased(filtered == 1 ? 1 : 2),
                    new EditPersonDescriptorBuilder(ALICE).build());

            assertThrows(CommandException.class, EditCommand.MESSAGE_DUPLICATE_PERSON, () -> command.execute(model));

            assertStoredData(ORIGINALS, model);
        }));
    }

    @Test
    public void execute_repeatedEdits_preservesOriginalId() throws CommandException {
        Model model = createModel(List.of(ORIGINALS.get(0)));
        PersonId originalId = ORIGINALS.get(0).getPersonId();
        for (int index = 0; index < 16; index++) {
            new EditCommand(Index.fromOneBased(1), new EditPersonDescriptorBuilder()
                    .withPhone("123" + index).build()).execute(model);
            assertEquals(originalId, model.getAddressBook().getPersonList().get(0).getPersonId());
            assertEquals("123" + index, model.getAddressBook().getPersonList().get(0).getPhone().value);
        }
    }

    private Model createModel(List<Person> persons) {
        AddressBook addressBook = new AddressBook();
        persons.forEach(addressBook::addPerson);
        return new ModelManager(addressBook, new UserPrefs());
    }

    private List<EditCase> editCases(Person person) {
        return List.of(
                new EditCase("omitted fields", new EditPersonDescriptor(), person),
                new EditCase("unchanged phone", new EditPersonDescriptorBuilder().withPhone(person.getPhone().value)
                        .build(), person),
                new EditCase("name", new EditPersonDescriptorBuilder().withName("Edited Name").build(),
                        new PersonBuilder(person).withName("Edited Name").build()),
                new EditCase("phone", new EditPersonDescriptorBuilder().withPhone("123456").build(),
                        new PersonBuilder(person).withPhone("123456").build()),
                new EditCase("email", new EditPersonDescriptorBuilder().withEmail("edited@example.com").build(),
                        new PersonBuilder(person).withEmail("edited@example.com").build()),
                new EditCase("address", new EditPersonDescriptorBuilder().withAddress("Edited Address").build(),
                        new PersonBuilder(person).withAddress("Edited Address").build()),
                new EditCase("empty tags", new EditPersonDescriptorBuilder().withTags().build(),
                        new PersonBuilder(person).withTags().build()),
                new EditCase("one tag", new EditPersonDescriptorBuilder().withTags("newtag").build(),
                        new PersonBuilder(person).withTags("newtag").build()),
                new EditCase("many tags", new EditPersonDescriptorBuilder().withTags("one", "two").build(),
                        new PersonBuilder(person).withTags("one", "two").build()),
                new EditCase("all fields", new EditPersonDescriptorBuilder().withName("Edited Name")
                        .withPhone("123456").withEmail("edited@example.com").withAddress("Edited Address")
                        .withTags("newtag").build(), new PersonBuilder(person).withName("Edited Name")
                        .withPhone("123456").withEmail("edited@example.com").withAddress("Edited Address")
                        .withTags("newtag").build()));
    }

    private void assertStoredData(List<Person> expected, Model model) {
        List<Person> actual = model.getAddressBook().getPersonList();
        assertEquals(expected.stream().map(Person::getPersonId).toList(),
                actual.stream().map(Person::getPersonId).toList());
        assertEquals(expected, actual);
        assertEquals(actual.size(), Set.copyOf(actual.stream().map(Person::getPersonId).toList()).size());
    }

    /** An edit partition with independently specified expected contact details. */
    private record EditCase(String name, EditPersonDescriptor descriptor, Person expected) {
    }
}
