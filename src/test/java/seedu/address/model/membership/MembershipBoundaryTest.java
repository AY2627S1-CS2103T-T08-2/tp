package seedu.address.model.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.model.membership.exceptions.DuplicateProjectMembershipException;
import seedu.address.model.membership.exceptions.ProjectMembershipNotFoundException;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.ProjectId;

/** Tests membership collection partitions independently of entity-reference validation. */
public class MembershipBoundaryTest {
    private static final List<ProjectMembership> PAIRS = IntStream.range(0, 9)
            .mapToObj(index -> new ProjectMembership(
                    PersonId.fromString("00000000-0000-0000-0000-00000000000" + (index / 3)),
                    ProjectId.fromString("00000000-0000-0000-0000-00000000000" + (index % 3)),
                    new Role("Role " + index)))
            .toList();

    @TestFactory
    public Stream<DynamicTest> operations_allThreeByThreeGraphs_preservePairIdentityAndRoles() {
        return IntStream.range(0, 512).mapToObj(mask -> dynamicTest("membership graph " + mask, () -> {
            List<ProjectMembership> expected = IntStream.range(0, 9)
                    .filter(index -> (mask & (1 << index)) != 0).mapToObj(PAIRS::get).toList();
            for (ProjectMembership pair : PAIRS) {
                UniqueProjectMembershipList list = new UniqueProjectMembershipList();
                list.setMemberships(expected);
                ProjectMembership changed = pair.withRole(new Role("Changed"));
                assertEquals(expected.contains(pair), list.contains(changed));
                if (expected.contains(pair)) {
                    assertThrows(DuplicateProjectMembershipException.class, () -> list.add(changed));
                    assertValues(expected, list);
                    list.set(changed);
                    assertEquals(new Role("Changed"),
                            list.asUnmodifiableObservableList().get(expected.indexOf(pair)).getRole());
                    list.remove(pair);
                    assertValues(expected.stream().filter(item -> !item.equals(pair)).toList(), list);
                } else {
                    assertThrows(ProjectMembershipNotFoundException.class, () -> list.remove(changed));
                    assertThrows(ProjectMembershipNotFoundException.class, () -> list.set(changed));
                    assertValues(expected, list);
                    list.add(changed);
                    List<ProjectMembership> appended = new ArrayList<>(expected);
                    appended.add(changed);
                    assertValues(appended, list);
                }
            }
        }));
    }

    @TestFactory
    public Stream<DynamicTest> setMemberships_aliasedInputs_preserveContents() {
        return IntStream.range(0, 2).mapToObj(mode -> dynamicTest("aliased membership list " + mode, () -> {
            UniqueProjectMembershipList list = new UniqueProjectMembershipList();
            list.setMemberships(PAIRS);
            if (mode == 0) {
                list.setMemberships(list);
            } else {
                list.setMemberships(list.asUnmodifiableObservableList());
            }
            assertValues(PAIRS, list);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> setMemberships_invalidElementAtEveryPosition_leavesOriginalUnchanged() {
        return IntStream.range(0, 10).boxed().flatMap(position -> IntStream.range(0, 2)
                .mapToObj(mode -> dynamicTest("invalid element " + position + " mode " + mode, () -> {
                    UniqueProjectMembershipList list = new UniqueProjectMembershipList();
                    list.setMemberships(PAIRS);
                    List<ProjectMembership> invalid = new ArrayList<>(PAIRS);
                    invalid.add(position, mode == 0 ? null : PAIRS.get(0).withRole(Role.EMPTY));
                    Class<? extends RuntimeException> exception = mode == 0
                            ? NullPointerException.class : DuplicateProjectMembershipException.class;
                    assertThrows(exception, () -> list.setMemberships(invalid));
                    assertValues(PAIRS, list);
                })));
    }

    @Test
    public void nullArgumentsAndReadOnlyViews_rejectMutation() {
        UniqueProjectMembershipList list = new UniqueProjectMembershipList();
        list.setMemberships(PAIRS);
        assertThrows(NullPointerException.class, () -> list.contains(null));
        assertThrows(NullPointerException.class, () -> list.add(null));
        assertThrows(NullPointerException.class, () -> list.set(null));
        assertThrows(NullPointerException.class, () -> list.remove(null));
        assertThrows(NullPointerException.class, () -> list.setMemberships((List<ProjectMembership>) null));
        assertThrows(NullPointerException.class, () -> list.setMemberships((UniqueProjectMembershipList) null));
        assertThrows(NullPointerException.class, () -> list.removeAllForPerson(null));
        assertThrows(NullPointerException.class, () -> list.removeAllForProject(null));
        var view = list.asUnmodifiableObservableList();
        assertThrows(UnsupportedOperationException.class, () -> view.set(0, PAIRS.get(1)));
        assertThrows(UnsupportedOperationException.class, () -> list.asUnmodifiableObservableList().add(PAIRS.get(0)));
        assertValues(PAIRS, list);
    }

    @TestFactory
    public Stream<DynamicTest> constructor_nullPartitions_rejectsMissingFields() {
        return IntStream.range(1, 8).mapToObj(mask -> dynamicTest("null constructor mask " + mask, () -> {
            ProjectMembership pair = PAIRS.get(0);
            PersonId personId = (mask & 1) == 0 ? pair.getPersonId() : null;
            ProjectId projectId = (mask & 2) == 0 ? pair.getProjectId() : null;
            Role role = (mask & 4) == 0 ? pair.getRole() : null;
            assertThrows(NullPointerException.class, () -> new ProjectMembership(personId, projectId, role));
        }));
    }

    @Test
    public void replacement_externalListMutation_doesNotChangeStoredMemberships() {
        UniqueProjectMembershipList list = new UniqueProjectMembershipList();
        List<ProjectMembership> input = new ArrayList<>(Arrays.asList(PAIRS.get(0), PAIRS.get(1)));
        list.setMemberships(input);
        input.clear();
        assertValues(PAIRS.subList(0, 2), list);
    }

    private static void assertValues(List<ProjectMembership> expected, UniqueProjectMembershipList actual) {
        assertEquals(expected, actual.asUnmodifiableObservableList());
        assertEquals(expected.stream().map(ProjectMembership::getRole).toList(),
                actual.asUnmodifiableObservableList().stream().map(ProjectMembership::getRole).toList());
    }
}
