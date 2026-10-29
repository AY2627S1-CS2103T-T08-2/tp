package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedProjectMembership.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE_ID;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.membership.Role;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.ProjectId;

public class JsonAdaptedProjectMembershipTest {

    private static final String VALID_PERSON_ID = ALICE_ID.toString();
    private static final String VALID_PROJECT_ID = "00000000-0000-4000-8000-000000000001";
    private static final String INVALID_ID = "not-a-uuid";
    private static final ProjectMembership MEMBERSHIP = new ProjectMembership(
        ALICE_ID, ProjectId.fromString(VALID_PROJECT_ID));

    @Test
    public void toModelType_validMembership_returnsMembership() throws Exception {
        JsonAdaptedProjectMembership membership = new JsonAdaptedProjectMembership(MEMBERSHIP);

        assertEquals(MEMBERSHIP, membership.toModelType());
        assertEquals(Role.EMPTY, membership
            .toModelType()
            .getRole());
    }

    @Test
    public void toModelType_legacyMembershipWithoutRole_usesEmptyRole() throws Exception {
        JsonAdaptedProjectMembership membership = new JsonAdaptedProjectMembership(
            VALID_PERSON_ID, VALID_PROJECT_ID, null);

        assertEquals(Role.EMPTY, membership
            .toModelType()
            .getRole());
    }

    @Test
    public void jsonRoundTrip_validMembership_preservesIds() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedProjectMembership(MEMBERSHIP));
        JsonAdaptedProjectMembership membership = JsonUtil.fromJsonString(
            json, JsonAdaptedProjectMembership.class);

        assertEquals(MEMBERSHIP, membership.toModelType());
    }

    @Test
    public void jsonRoundTrip_membershipWithRole_preservesRole() throws Exception {
        ProjectMembership membershipWithRole = MEMBERSHIP.withRole(new Role("Implement parser"));
        String json = JsonUtil.toJsonString(new JsonAdaptedProjectMembership(membershipWithRole));
        JsonAdaptedProjectMembership adaptedMembership = JsonUtil.fromJsonString(
            json, JsonAdaptedProjectMembership.class);

        assertEquals(membershipWithRole, adaptedMembership.toModelType());
        assertEquals(new Role("Implement parser"), adaptedMembership
            .toModelType()
            .getRole());
    }

    @Test
    public void toModelType_invalidPersonId_throwsIllegalValueException() {
        JsonAdaptedProjectMembership membership = new JsonAdaptedProjectMembership(INVALID_ID, VALID_PROJECT_ID, null);

        assertThrows(IllegalValueException.class, PersonId.MESSAGE_CONSTRAINTS, membership::toModelType);
    }

    @Test
    public void toModelType_nullPersonId_throwsIllegalValueException() {
        JsonAdaptedProjectMembership membership = new JsonAdaptedProjectMembership(null, VALID_PROJECT_ID, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, PersonId.class.getSimpleName());

        assertThrows(IllegalValueException.class, expectedMessage, membership::toModelType);
    }

    @Test
    public void toModelType_invalidProjectId_throwsIllegalValueException() {
        JsonAdaptedProjectMembership membership = new JsonAdaptedProjectMembership(VALID_PERSON_ID, INVALID_ID, null);

        assertThrows(IllegalValueException.class, ProjectId.MESSAGE_CONSTRAINTS, membership::toModelType);
    }

    @Test
    public void toModelType_nullProjectId_throwsIllegalValueException() {
        JsonAdaptedProjectMembership membership = new JsonAdaptedProjectMembership(VALID_PERSON_ID, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, ProjectId.class.getSimpleName());

        assertThrows(IllegalValueException.class, expectedMessage, membership::toModelType);
    }

    @Test
    public void toModelType_invalidRole_throwsIllegalValueException() {
        JsonAdaptedProjectMembership membership = new JsonAdaptedProjectMembership(
            VALID_PERSON_ID, VALID_PROJECT_ID, "line one\nline two");

        assertThrows(IllegalValueException.class, Role.MESSAGE_CONSTRAINTS, membership::toModelType);
    }

    @Test
    public void toModelType_controlCharacterRole_throwsIllegalValueException() {
        for (char lineBreak : new char[] {11, '\f'}) {
            JsonAdaptedProjectMembership membership = new JsonAdaptedProjectMembership(
                VALID_PERSON_ID, VALID_PROJECT_ID, "Team" + lineBreak + "Leader");
            assertThrows(IllegalValueException.class, Role.MESSAGE_CONSTRAINTS, membership::toModelType);
        }
    }
}
