package seedu.address.model.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.PersonId;
import seedu.address.model.project.ProjectId;

public class ProjectMembershipTest {

    private static final PersonId PERSON_ID =
            PersonId.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final PersonId OTHER_PERSON_ID =
            PersonId.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final ProjectId PROJECT_ID =
            ProjectId.fromString("00000000-0000-4000-8000-000000000001");
    private static final ProjectId OTHER_PROJECT_ID =
            ProjectId.fromString("00000000-0000-4000-8000-000000000002");

    @Test
    public void constructor_nullPersonId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ProjectMembership(null, PROJECT_ID));
    }

    @Test
    public void constructor_nullProjectId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ProjectMembership(PERSON_ID, null));
    }

    @Test
    public void getters_validIds_returnSuppliedIds() {
        ProjectMembership membership = new ProjectMembership(PERSON_ID, PROJECT_ID);

        assertEquals(PERSON_ID, membership.getPersonId());
        assertEquals(PROJECT_ID, membership.getProjectId());
        assertEquals(Role.EMPTY, membership.getRole());
    }

    @Test
    public void withRole_replacesRoleAndPreservesMembershipIdentity() {
        ProjectMembership membership = new ProjectMembership(PERSON_ID, PROJECT_ID);
        Role role = new Role("Write tests");

        ProjectMembership updatedMembership = membership.withRole(role);

        assertEquals(role, updatedMembership.getRole());
        assertEquals(membership, updatedMembership);
    }

    @Test
    public void constructor_nullRole_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ProjectMembership(PERSON_ID, PROJECT_ID, null));
    }

    @Test
    public void withRole_nullRole_throwsNullPointerException() {
        ProjectMembership membership = new ProjectMembership(PERSON_ID, PROJECT_ID);

        assertThrows(NullPointerException.class, () -> membership.withRole(null));
    }

    @Test
    public void equals_sameIds_returnsTrue() {
        ProjectMembership membership = new ProjectMembership(PERSON_ID, PROJECT_ID);
        ProjectMembership membershipWithSameIds = new ProjectMembership(PERSON_ID, PROJECT_ID);

        assertEquals(membership, membershipWithSameIds);
    }

    @Test
    public void hashCode_sameIds_returnsSameHashCode() {
        ProjectMembership membership = new ProjectMembership(PERSON_ID, PROJECT_ID);
        ProjectMembership membershipWithSameIds = new ProjectMembership(PERSON_ID, PROJECT_ID);

        assertEquals(membership.hashCode(), membershipWithSameIds.hashCode());
    }

    @Test
    public void equals_differentPersonId_returnsFalse() {
        ProjectMembership membership = new ProjectMembership(PERSON_ID, PROJECT_ID);
        ProjectMembership membershipWithDifferentPerson =
                new ProjectMembership(OTHER_PERSON_ID, PROJECT_ID);

        assertNotEquals(membership, membershipWithDifferentPerson);
    }

    @Test
    public void equals_differentProjectId_returnsFalse() {
        ProjectMembership membership = new ProjectMembership(PERSON_ID, PROJECT_ID);
        ProjectMembership membershipWithDifferentProject =
                new ProjectMembership(PERSON_ID, OTHER_PROJECT_ID);

        assertNotEquals(membership, membershipWithDifferentProject);
    }

    @Test
    public void equals_nullOrDifferentType_returnsFalse() {
        ProjectMembership membership = new ProjectMembership(PERSON_ID, PROJECT_ID);

        assertFalse(membership.equals(null));
        assertFalse(membership.equals(PERSON_ID));
    }
}
