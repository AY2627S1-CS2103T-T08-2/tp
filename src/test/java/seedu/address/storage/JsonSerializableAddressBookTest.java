package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectId;
import seedu.address.testutil.ProjectBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");
    private static final Path VALID_PERSON_IDS_FILE = TEST_DATA_FOLDER.resolve("validPersonIdsAddressBook.json");
    private static final Path DUPLICATE_PERSON_IDS_FILE =
            TEST_DATA_FOLDER.resolve("duplicatePersonIdsAddressBook.json");
    private static final Path DUPLICATE_PERSON_IDS_DIFFERENT_CASE_FILE =
            TEST_DATA_FOLDER.resolve("duplicatePersonIdsDifferentCaseAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
        assertEquals(typicalPersonsAddressBook.getPersonList().stream().map(Person::getPersonId).toList(),
                addressBookFromFile.getPersonList().stream().map(Person::getPersonId).toList());
    }

    @Test
    public void toJsonString_typicalPersons_matchesFixtureIds() throws Exception {
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(typicalPersonsAddressBook));
        JsonNode persons = JsonUtil.fromJsonString(json, JsonNode.class).get("persons");
        JsonNode expectedPersons = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE, JsonNode.class).get().get("persons");

        assertEquals(expectedPersons.size(), persons.size());
        for (int i = 0; i < persons.size(); i++) {
            assertEquals(expectedPersons.get(i).get("id"), persons.get(i).get("id"));
        }
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_validPersonIds_preservesIds() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(VALID_PERSON_IDS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();

        assertEquals(2, addressBookFromFile.getPersonList().size());
        assertEquals(PersonId.fromString("550e8400-e29b-41d4-a716-446655440001"),
                addressBookFromFile.getPersonList().get(0).getPersonId());
        assertEquals(PersonId.fromString("550e8400-e29b-41d4-a716-446655440002"),
                addressBookFromFile.getPersonList().get(1).getPersonId());
    }

    @Test
    public void toModelType_duplicatePersonIds_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_IDS_FILE,
                JsonSerializableAddressBook.class).get();

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON_ID,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersonIdsDifferentCase_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_IDS_DIFFERENT_CASE_FILE,
                JsonSerializableAddressBook.class).get();

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON_ID,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateNamesAndIds_reportsDuplicatePerson() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(
                new JsonAdaptedPerson(TypicalPersons.ALICE), new JsonAdaptedPerson(TypicalPersons.ALICE)),
                List.of(), List.of());

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                data::toModelType);
    }

    @Test
    public void toModelType_legacyFileWithoutProjects_returnsEmptyProjectList() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();

        assertEquals(List.of(), dataFromFile.toModelType().getProjectList());
    }

    @Test
    public void toModelType_jsonWithoutPersons_returnsEmptyPersonList() throws Exception {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(null, List.of(), List.of());

        assertEquals(List.of(), data.toModelType().getPersonList());
    }

    @Test
    public void toModelType_validProjects_preservesProjectIdsAndDetails() throws Exception {
        Project project = new ProjectBuilder().withName("CS2103T Project").build();
        AddressBook addressBook = TypicalPersons.getTypicalAddressBook();
        addressBook.addProject(project);

        AddressBook restored = new JsonSerializableAddressBook(addressBook).toModelType();

        assertEquals(List.of(project), restored.getProjectList());
        assertEquals(project.getProjectId(), restored.getProjectList().get(0).getProjectId());
    }

    @Test
    public void toModelType_duplicateProjectIds_throwsIllegalValueException() {
        Project original = new ProjectBuilder().withName("Project Alpha").build();
        Project duplicateId = new ProjectBuilder()
                .withProjectId(original.getProjectId())
                .withName("Project Beta")
                .build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(), List.of(
                new JsonAdaptedProject(original), new JsonAdaptedProject(duplicateId)), List.of());

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PROJECT_ID,
                data::toModelType);
    }

    @Test
    public void toModelType_duplicateProjectNames_throwsIllegalValueException() {
        Project original = new ProjectBuilder().withName("Project Alpha").build();
        Project duplicateName = new ProjectBuilder()
                .withProjectId(ProjectId.generate())
                .withName("project alpha")
                .build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(), List.of(
                new JsonAdaptedProject(original), new JsonAdaptedProject(duplicateName)), List.of());

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PROJECT,
                data::toModelType);
    }

    @Test
    public void toModelType_legacyFileWithoutMemberships_returnsEmptyMembershipList() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();

        assertEquals(List.of(), dataFromFile.toModelType().getProjectMembershipList());
    }

    @Test
    public void toModelType_validMembership_preservesAssociation() throws Exception {
        AddressBook addressBook = TypicalPersons.getTypicalAddressBook();
        Project project = new ProjectBuilder().withName("CS2103T Project").build();
        ProjectMembership membership = new ProjectMembership(
                TypicalPersons.ALICE.getPersonId(), project.getProjectId());
        addressBook.addProject(project);
        addressBook.addProjectMembership(membership);

        AddressBook restored = new JsonSerializableAddressBook(addressBook).toModelType();

        assertEquals(List.of(membership), restored.getProjectMembershipList());
    }

    @Test
    public void toModelType_duplicateMemberships_throwsIllegalValueException() {
        Project project = new ProjectBuilder().withName("CS2103T Project").build();
        ProjectMembership membership = new ProjectMembership(
                TypicalPersons.ALICE.getPersonId(), project.getProjectId());
        JsonAdaptedProjectMembership adaptedMembership = new JsonAdaptedProjectMembership(membership);
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(TypicalPersons.ALICE)),
                List.of(new JsonAdaptedProject(project)),
                List.of(adaptedMembership, adaptedMembership));

        assertThrows(IllegalValueException.class,
                JsonSerializableAddressBook.MESSAGE_DUPLICATE_PROJECT_MEMBERSHIP, data::toModelType);
    }

    @Test
    public void toModelType_membershipWithMissingPerson_throwsIllegalValueException() {
        Project project = new ProjectBuilder().withName("CS2103T Project").build();
        ProjectMembership membership = new ProjectMembership(PersonId.generate(), project.getProjectId());
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(),
                List.of(new JsonAdaptedProject(project)),
                List.of(new JsonAdaptedProjectMembership(membership)));

        assertThrows(IllegalValueException.class,
                JsonSerializableAddressBook.MESSAGE_MEMBERSHIP_PERSON_NOT_FOUND, data::toModelType);
    }

    @Test
    public void toModelType_membershipWithMissingProject_throwsIllegalValueException() {
        ProjectMembership membership = new ProjectMembership(
                TypicalPersons.ALICE.getPersonId(), ProjectId.generate());
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(TypicalPersons.ALICE)),
                List.of(),
                List.of(new JsonAdaptedProjectMembership(membership)));

        assertThrows(IllegalValueException.class,
                JsonSerializableAddressBook.MESSAGE_MEMBERSHIP_PROJECT_NOT_FOUND, data::toModelType);
    }

}
