package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectContact;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedProject> projects = new ArrayList<>();
    private final List<JsonAdaptedProjectContact> projectContacts = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("projects") List<JsonAdaptedProject> projects,
            @JsonProperty("projectContacts") List<JsonAdaptedProjectContact> projectContacts) {
        if (persons != null) {
            this.persons.addAll(persons);
        }
        if (projects != null) {
            this.projects.addAll(projects);
        }
        if (projectContacts != null) {
            this.projectContacts.addAll(projectContacts);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        projects.addAll(source.getProjectList().stream().map(JsonAdaptedProject::new).collect(Collectors.toList()));
        projectContacts.addAll(source.getProjectContacts().stream()
                .map(JsonAdaptedProjectContact::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            addressBook.addPerson(person);
        }
        Set<Integer> projectIds = new HashSet<>();
        List<Project> modelProjects = new ArrayList<>();
        for (JsonAdaptedProject adaptedProject : projects) {
            Project project = adaptedProject.toModelType();
            if (!projectIds.add(project.getId())) {
                throw new IllegalValueException("Projects list contains duplicate project ids.");
            }
            modelProjects.add(project);
        }
        Set<String> personNames = addressBook.getPersonList().stream()
                .map(person -> person.getName().fullName).collect(Collectors.toSet());
        Set<String> uniqueLinks = new HashSet<>();
        List<ProjectContact> modelLinks = new ArrayList<>();
        for (JsonAdaptedProjectContact adaptedLink : projectContacts) {
            ProjectContact link = adaptedLink.toModelType();
            if (!projectIds.contains(link.getProjectId()) || !personNames.contains(link.getContactName())) {
                throw new IllegalValueException("Project-contact association refers to an unknown project or contact.");
            }
            if (!uniqueLinks.add(link.getProjectId() + "\u0000" + link.getContactName())) {
                throw new IllegalValueException("Project-contact associations contain duplicates.");
            }
            modelLinks.add(link);
        }
        addressBook.setProjects(modelProjects);
        addressBook.setProjectContacts(modelLinks);
        return addressBook;
    }

}
