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
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectId;
import seedu.address.model.project.ProjectName;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_PERSON_ID = "Persons list contains duplicate person ID(s).";
    public static final String MESSAGE_DUPLICATE_PROJECT = "Projects list contains duplicate project(s).";
    public static final String MESSAGE_DUPLICATE_PROJECT_ID = "Projects list contains duplicate project ID(s).";
    public static final String MESSAGE_DUPLICATE_PROJECT_MEMBERSHIP =
            "Project memberships list contains duplicate membership(s).";
    public static final String MESSAGE_MEMBERSHIP_PERSON_NOT_FOUND =
            "Project membership references a person that does not exist.";
    public static final String MESSAGE_MEMBERSHIP_PROJECT_NOT_FOUND =
            "Project membership references a project that does not exist.";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedProject> projects = new ArrayList<>();
    private final List<JsonAdaptedProjectMembership> memberships = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons, projects, and memberships.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("projects") List<JsonAdaptedProject> projects,
            @JsonProperty("memberships") List<JsonAdaptedProjectMembership> memberships) {
        if (persons != null) {
            this.persons.addAll(persons);
        }
        if (projects != null) {
            this.projects.addAll(projects);
        }
        if (memberships != null) {
            this.memberships.addAll(memberships);
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
        memberships.addAll(source.getProjectMembershipList().stream()
                .map(JsonAdaptedProjectMembership::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        Set<PersonId> personIds = new HashSet<>();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            if (!personIds.add(person.getPersonId())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON_ID);
            }
            addressBook.addPerson(person);
        }

        Set<ProjectId> projectIds = new HashSet<>();
        Set<ProjectName> projectNames = new HashSet<>();
        for (JsonAdaptedProject jsonAdaptedProject : projects) {
            Project project = jsonAdaptedProject.toModelType();
            if (!projectIds.add(project.getProjectId())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PROJECT_ID);
            }
            if (!projectNames.add(project.getName())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PROJECT);
            }
            addressBook.addProject(project);
        }

        for (JsonAdaptedProjectMembership jsonAdaptedMembership : memberships) {
            ProjectMembership membership = jsonAdaptedMembership.toModelType();
            if (!personIds.contains(membership.getPersonId())) {
                throw new IllegalValueException(MESSAGE_MEMBERSHIP_PERSON_NOT_FOUND);
            }
            if (!projectIds.contains(membership.getProjectId())) {
                throw new IllegalValueException(MESSAGE_MEMBERSHIP_PROJECT_NOT_FOUND);
            }
            if (addressBook.hasProjectMembership(membership)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PROJECT_MEMBERSHIP);
            }
            addressBook.addProjectMembership(membership);
        }
        return addressBook;
    }

}
