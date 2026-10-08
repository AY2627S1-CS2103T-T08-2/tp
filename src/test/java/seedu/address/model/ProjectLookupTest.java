package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectContact;
import seedu.address.testutil.TypicalPersons;

class ProjectLookupTest {
    @Test
    void lookupsSupportManyToManyAssociations() {
        AddressBook addressBook = new AddressBook();
        Person alice = TypicalPersons.ALICE;
        Person benson = TypicalPersons.BENSON;
        addressBook.addPerson(alice);
        addressBook.addPerson(benson);
        Project first = new Project(1, "UniTeam");
        Project second = new Project(2, "Navigation");
        addressBook.setProjects(List.of(second, first));
        addressBook.setProjectContacts(List.of(new ProjectContact(1, alice.getName().fullName),
                new ProjectContact(1, benson.getName().fullName),
                new ProjectContact(2, alice.getName().fullName)));
        Model model = new ModelManager(addressBook, new UserPrefs());

        assertEquals(List.of(first, second), model.getProjects());
        assertEquals(List.of(alice, benson), model.getContactsForProject(1));
        assertEquals(List.of(first, second), model.getProjectsForContact(alice.getName().fullName));
        assertEquals(List.of(), model.getContactsForProject(99));
        assertEquals(List.of(), model.getProjectsForContact("Unknown"));
    }
}
