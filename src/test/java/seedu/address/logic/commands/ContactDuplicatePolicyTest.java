package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.ContactDuplicateTestData.ALL_FIELDS;
import static seedu.address.testutil.ContactDuplicateTestData.BASE;
import static seedu.address.testutil.ContactDuplicateTestData.NAME;
import static seedu.address.testutil.ContactDuplicateTestData.SECOND_ID;
import static seedu.address.testutil.ContactDuplicateTestData.UNRELATED;
import static seedu.address.testutil.ContactDuplicateTestData.addCommand;
import static seedu.address.testutil.ContactDuplicateTestData.contactOrder;
import static seedu.address.testutil.ContactDuplicateTestData.person;
import static seedu.address.testutil.ContactDuplicateTestData.restoreBaseFields;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.Project;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.ProjectBuilder;

/** Tests duplicate policy through command parsing and real filtered models. */
public class ContactDuplicatePolicyTest {
    @TestFactory
    public Stream<DynamicTest> execute_exactDuplicateWithZeroOneOrManyTags_rejectsVisibleAndHiddenDuplicate() {
        List<String[]> tagSets = List.of(new String[] {}, new String[] {"friend"},
                new String[] {"friend", "classmate"});
        return IntStream.range(0, tagSets.size()).boxed().flatMap(tagCount ->
                IntStream.range(0, 2).mapToObj(hidden -> dynamicTest("tags " + tagCount + " hidden " + hidden, () -> {
                    Person first = new PersonBuilder(BASE).withTags(tagSets.get(tagCount)).build();
                    Model model = createModel(List.of(UNRELATED, first));
                    if (hidden == 1) {
                        model.updateFilteredPersonList(contact ->
                                contact.getPersonId().equals(UNRELATED.getPersonId()));
                    }
                    Snapshot before = snapshot(model);
                    Command command = new AddressBookParser().parseCommand(addCommand(first));

                    assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () ->
                            command.execute(model));

                    assertUnchanged(before, model);
                })));
    }

    @Test
    public void execute_repeatingSuccessfulAdd_rejectsSecondAttemptWithoutChangingData() throws Exception {
        Model model = createModel(List.of(BASE));
        String text = addCommand(person(ALL_FIELDS & ~NAME, SECOND_ID));
        new AddressBookParser().parseCommand(text).execute(model);
        model.updateFilteredPersonList(contact -> false);
        Snapshot before = snapshot(model);
        Command duplicate = new AddressBookParser().parseCommand(text);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> duplicate.execute(model));

        assertUnchanged(before, model);
    }

    @TestFactory
    public Stream<DynamicTest> execute_addAllFieldAndFilterPartitions_checksCompleteAddressBook() {
        return IntStream.rangeClosed(0, ALL_FIELDS).boxed().flatMap(differences ->
                IntStream.range(0, 3).mapToObj(filter ->
                        dynamicTest("add fields " + differences + " filter " + filter, () -> {
                            Model model = createModel(List.of(UNRELATED, BASE));
                            if (filter == 1) {
                                model.updateFilteredPersonList(contact ->
                                        contact.getPersonId().equals(UNRELATED.getPersonId()));
                            } else if (filter == 2) {
                                model.updateFilteredPersonList(contact -> false);
                            }
                            Snapshot before = snapshot(model);
                            Person candidate = person(differences, SECOND_ID);
                            Command command = new AddressBookParser().parseCommand(addCommand(candidate));

                            if (differences == 0) {
                                assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () ->
                                        command.execute(model));
                                assertUnchanged(before, model);
                            } else {
                                CommandResult result = command.execute(model);
                                List<Person> actual = model.getAddressBook().getPersonList();
                                assertEquals(List.of(UNRELATED, BASE, candidate), actual);
                                assertEquals(List.of(UNRELATED.getPersonId(), BASE.getPersonId()),
                                        actual.subList(0, 2).stream().map(Person::getPersonId).toList());
                                assertFalse(before.data().getPersonList().stream().anyMatch(contact ->
                                        contact.getPersonId().equals(actual.get(2).getPersonId())));
                                assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(actual.get(2))),
                                        result.getFeedbackToUser());
                                assertEquals(actual, model.getFilteredPersonList());
                                assertAssociationsUnchanged(before.data(), model.getAddressBook());
                            }
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> execute_sameNameEdits_allDifferenceAndRestorationSubsets() {
        return IntStream.rangeClosed(1, ALL_FIELDS).filter(differences -> (differences & NAME) == 0).boxed()
                .flatMap(differences -> IntStream.rangeClosed(0, ALL_FIELDS)
                        .filter(restored -> (restored & differences) == restored).boxed()
                        .flatMap(restored -> IntStream.range(0, 4).mapToObj(context ->
                                dynamicTest("edit differences " + differences + " restored " + restored
                                        + " context " + context, () -> {
                                            Person target = person(differences, SECOND_ID);
                                            int order = context % 2;
                                            boolean filtered = context >= 2;
                                            List<Person> original = contactOrder(target, order);
                                            Model model = createModel(original);
                                            if (filtered) {
                                                model.updateFilteredPersonList(contact ->
                                                        contact.getPersonId().equals(SECOND_ID));
                                            }
                                            Snapshot before = snapshot(model);
                                            Index index = Index.fromOneBased(filtered || order == 0 ? 1 : 3);
                                            EditCommand command = new EditCommand(index, restoreBaseFields(restored));
                                            int remaining = differences & ~restored;

                                            if (remaining == 0) {
                                                assertThrows(CommandException.class,
                                                        EditCommand.MESSAGE_DUPLICATE_PERSON, () ->
                                                                command.execute(model));
                                                assertUnchanged(before, model);
                                            } else {
                                                Person edited = person(remaining, SECOND_ID);
                                                List<Person> expected = new ArrayList<>(original);
                                                expected.set(order == 0 ? 0 : 2, edited);
                                                CommandResult result = command.execute(model);
                                                assertContacts(expected, model.getAddressBook());
                                                assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                                                        Messages.format(edited)), result.getFeedbackToUser());
                                                assertAssociationsUnchanged(before.data(), model.getAddressBook());
                                            }
                                        }))));
    }

    @TestFactory
    public Stream<DynamicTest> execute_addTagOrderRepetitionsAndCardinality_comparesSets() {
        List<String> tags = List.of("", " t/friend", " t/classmate t/friend", " t/friend t/classmate",
                " t/friend t/classmate t/friend", " t/friend t/classmate t/colleague", " t/Friend t/classmate");
        String fields = addCommand(new PersonBuilder(BASE).withTags().build());
        return IntStream.range(0, tags.size()).mapToObj(partition -> dynamicTest("add tags " + partition, () -> {
            Model model = createModel(List.of(BASE));
            Snapshot before = snapshot(model);
            Command command = new AddressBookParser().parseCommand(fields + tags.get(partition));

            if (partition >= 2 && partition <= 4) {
                assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> command.execute(model));
                assertUnchanged(before, model);
            } else {
                command.execute(model);
                assertEquals(2, model.getAddressBook().getPersonList().size());
                assertEquals(BASE.getPersonId(), model.getAddressBook().getPersonList().get(0).getPersonId());
                assertAssociationsUnchanged(before.data(), model.getAddressBook());
            }
        }));
    }

    @TestFactory
    public Stream<DynamicTest> execute_clearTags_handlesLastDifferenceAndNoOp() {
        return IntStream.range(0, 2).mapToObj(partition -> dynamicTest("clear tags partition " + partition, () -> {
            Person first = new PersonBuilder(BASE).withTags().build();
            Person target = new PersonBuilder(BASE).withPersonId(SECOND_ID).withTags("friend").build();
            if (partition == 1) {
                first = BASE;
                target = new PersonBuilder(BASE).withPersonId(SECOND_ID).withTags().build();
            }
            Model model = createModel(List.of(first, target));
            model.updateFilteredPersonList(contact -> contact.getPersonId().equals(SECOND_ID));
            Snapshot before = snapshot(model);
            Command command = new AddressBookParser().parseCommand("edit 1 t/");

            if (partition == 0) {
                assertThrows(CommandException.class, EditCommand.MESSAGE_DUPLICATE_PERSON, () ->
                        command.execute(model));
                assertUnchanged(before, model);
            } else {
                command.execute(model);
                assertContacts(before.data().getPersonList(), model.getAddressBook());
                assertAssociationsUnchanged(before.data(), model.getAddressBook());
            }
        }));
    }

    private Model createModel(List<Person> persons) {
        AddressBook data = new AddressBook();
        persons.forEach(data::addPerson);
        Project project = new ProjectBuilder().build();
        data.addProject(project);
        for (int index = 0; index < persons.size(); index++) {
            data.addProjectMembership(new ProjectMembership(persons.get(index).getPersonId(), project.getProjectId(),
                    new Role("Task " + index)));
        }
        return new ModelManager(data, new UserPrefs());
    }

    private Snapshot snapshot(Model model) {
        return new Snapshot(new AddressBook(model.getAddressBook()),
                model.getFilteredPersonList().stream().map(Person::getPersonId).toList());
    }

    private void assertUnchanged(Snapshot before, Model model) {
        assertContacts(before.data().getPersonList(), model.getAddressBook());
        assertAssociationsUnchanged(before.data(), model.getAddressBook());
        assertEquals(before.filteredIds(), model.getFilteredPersonList().stream().map(Person::getPersonId).toList());
    }

    private void assertContacts(List<Person> expected, ReadOnlyAddressBook actual) {
        assertEquals(expected, actual.getPersonList());
        assertEquals(expected.stream().map(Person::getPersonId).toList(),
                actual.getPersonList().stream().map(Person::getPersonId).toList());
        Set<PersonId> actualIds = Set.copyOf(actual.getPersonList().stream().map(Person::getPersonId).toList());
        assertEquals(expected.size(), actualIds.size());
    }

    private void assertAssociationsUnchanged(ReadOnlyAddressBook expected, ReadOnlyAddressBook actual) {
        assertEquals(expected.getProjectList(), actual.getProjectList());
        assertEquals(expected.getProjectMembershipList(), actual.getProjectMembershipList());
        assertEquals(expected.getProjectMembershipList().stream().map(ProjectMembership::getRole).toList(),
                actual.getProjectMembershipList().stream().map(ProjectMembership::getRole).toList());
    }

    /** Model data and displayed IDs captured before a command. */
    private record Snapshot(ReadOnlyAddressBook data, List<PersonId> filteredIds) {
    }
}
