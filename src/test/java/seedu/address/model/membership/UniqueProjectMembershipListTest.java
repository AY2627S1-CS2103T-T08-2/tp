package seedu.address.model.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.membership.exceptions.DuplicateProjectMembershipException;
import seedu.address.model.membership.exceptions.ProjectMembershipNotFoundException;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.ProjectId;

public class UniqueProjectMembershipListTest {

    private static final PersonId PERSON_ID =
            PersonId.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final PersonId OTHER_PERSON_ID =
            PersonId.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final ProjectId PROJECT_ID =
            ProjectId.fromString("00000000-0000-4000-8000-000000000001");
    private static final ProjectId OTHER_PROJECT_ID =
            ProjectId.fromString("00000000-0000-4000-8000-000000000002");
    private static final ProjectMembership MEMBERSHIP = new ProjectMembership(PERSON_ID, PROJECT_ID);
    private static final ProjectMembership SAME_PERSON_OTHER_PROJECT =
            new ProjectMembership(PERSON_ID, OTHER_PROJECT_ID);
    private static final ProjectMembership OTHER_PERSON_SAME_PROJECT =
            new ProjectMembership(OTHER_PERSON_ID, PROJECT_ID);

    private final UniqueProjectMembershipList memberships = new UniqueProjectMembershipList();

    @Test
    public void contains_nullMembership_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> memberships.contains(null));
    }

    @Test
    public void contains_membershipNotInList_returnsFalse() {
        assertFalse(memberships.contains(MEMBERSHIP));
    }

    @Test
    public void contains_membershipInList_returnsTrue() {
        memberships.add(MEMBERSHIP);

        assertTrue(memberships.contains(new ProjectMembership(PERSON_ID, PROJECT_ID)));
    }

    @Test
    public void add_nullMembership_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> memberships.add(null));
    }

    @Test
    public void add_duplicateMembership_throwsDuplicateProjectMembershipException() {
        memberships.add(MEMBERSHIP);

        assertThrows(DuplicateProjectMembershipException.class, () -> memberships.add(MEMBERSHIP));
        assertEquals(List.of(MEMBERSHIP), memberships.asUnmodifiableObservableList());
    }

    @Test
    public void add_samePersonOrProjectDifferentPair_success() {
        memberships.add(MEMBERSHIP);
        memberships.add(SAME_PERSON_OTHER_PROJECT);
        memberships.add(OTHER_PERSON_SAME_PROJECT);

        assertEquals(List.of(MEMBERSHIP, SAME_PERSON_OTHER_PROJECT, OTHER_PERSON_SAME_PROJECT),
                memberships.asUnmodifiableObservableList());
    }

    @Test
    public void remove_nullMembership_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> memberships.remove(null));
    }

    @Test
    public void remove_missingMembership_throwsProjectMembershipNotFoundException() {
        assertThrows(ProjectMembershipNotFoundException.class, () -> memberships.remove(MEMBERSHIP));
    }

    @Test
    public void remove_existingMembership_removesMembership() {
        memberships.add(MEMBERSHIP);

        memberships.remove(new ProjectMembership(PERSON_ID, PROJECT_ID));

        assertTrue(memberships.asUnmodifiableObservableList().isEmpty());
    }

    @Test
    public void set_nullMembership_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> memberships.set(null));
    }

    @Test
    public void set_existingMembership_replacesValueAndKeepsPosition() {
        memberships.add(MEMBERSHIP);
        memberships.add(SAME_PERSON_OTHER_PROJECT);
        ProjectMembership updatedMembership = MEMBERSHIP.withRole(new Role("Write tests"));

        memberships.set(updatedMembership);

        assertEquals(List.of(updatedMembership, SAME_PERSON_OTHER_PROJECT), memberships.asUnmodifiableObservableList());
    }

    @Test
    public void set_missingMembership_throwsProjectMembershipNotFoundException() {
        assertThrows(ProjectMembershipNotFoundException.class, () -> memberships.set(MEMBERSHIP));
    }

    @Test
    public void setMemberships_nullList_throwsNullPointerException() {
        List<ProjectMembership> nullMemberships = null;

        assertThrows(NullPointerException.class, () -> memberships.setMemberships(nullMemberships));
    }

    @Test
    public void setMemberships_listWithDuplicateMemberships_throwsDuplicateProjectMembershipException() {
        memberships.add(SAME_PERSON_OTHER_PROJECT);
        List<ProjectMembership> duplicateMemberships = List.of(MEMBERSHIP, MEMBERSHIP);

        assertThrows(DuplicateProjectMembershipException.class, () -> memberships.setMemberships(duplicateMemberships));
        assertEquals(List.of(SAME_PERSON_OTHER_PROJECT), memberships.asUnmodifiableObservableList());
    }

    @Test
    public void setMemberships_samePersonProjectWithDifferentRoles_throwsDuplicateProjectMembershipException() {
        ProjectMembership firstRole = MEMBERSHIP.withRole(new Role("Implement parser"));
        ProjectMembership secondRole = MEMBERSHIP.withRole(new Role("Write tests"));
        assertThrows(DuplicateProjectMembershipException.class, () -> {
            memberships.setMemberships(List.of(firstRole, secondRole));
        });
        assertTrue(memberships.asUnmodifiableObservableList().isEmpty());
    }

    @Test
    public void setMemberships_validList_replacesMemberships() {
        memberships.add(MEMBERSHIP);

        memberships.setMemberships(List.of(SAME_PERSON_OTHER_PROJECT, OTHER_PERSON_SAME_PROJECT));

        assertEquals(List.of(SAME_PERSON_OTHER_PROJECT, OTHER_PERSON_SAME_PROJECT),
                memberships.asUnmodifiableObservableList());
    }

    @Test
    public void setMemberships_selfReplacement_preservesMemberships() {
        memberships.add(MEMBERSHIP);
        memberships.add(SAME_PERSON_OTHER_PROJECT);

        memberships.setMemberships(memberships);

        assertEquals(List.of(MEMBERSHIP, SAME_PERSON_OTHER_PROJECT), memberships.asUnmodifiableObservableList());
    }

    @Test
    public void removeAllForPerson_matchingMemberships_removesOnlyMatches() {
        memberships.setMemberships(List.of(MEMBERSHIP, SAME_PERSON_OTHER_PROJECT, OTHER_PERSON_SAME_PROJECT));

        memberships.removeAllForPerson(PERSON_ID);

        assertEquals(List.of(OTHER_PERSON_SAME_PROJECT), memberships.asUnmodifiableObservableList());
    }

    @Test
    public void removeAllForProject_matchingMemberships_removesOnlyMatches() {
        memberships.setMemberships(List.of(MEMBERSHIP, SAME_PERSON_OTHER_PROJECT, OTHER_PERSON_SAME_PROJECT));

        memberships.removeAllForProject(PROJECT_ID);

        assertEquals(List.of(SAME_PERSON_OTHER_PROJECT), memberships.asUnmodifiableObservableList());
    }

    @Test
    public void cleanup_nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> memberships.removeAllForPerson(null));
        assertThrows(NullPointerException.class, () -> memberships.removeAllForProject(null));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        memberships.add(MEMBERSHIP);
        var unmodifiableMemberships = memberships.asUnmodifiableObservableList();

        assertThrows(UnsupportedOperationException.class, () -> unmodifiableMemberships.clear());
    }

    @Test
    public void iterator_remove_throwsUnsupportedOperationException() {
        memberships.add(MEMBERSHIP);
        Iterator<ProjectMembership> iterator = memberships.iterator();
        iterator.next();

        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
}
