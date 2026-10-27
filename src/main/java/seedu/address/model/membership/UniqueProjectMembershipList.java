package seedu.address.model.membership;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.membership.exceptions.DuplicateProjectMembershipException;
import seedu.address.model.membership.exceptions.ProjectMembershipNotFoundException;
import seedu.address.model.person.PersonId;
import seedu.address.model.project.ProjectId;

/**
 * A list of project memberships that enforces unique person-project pairs and does not allow nulls.
 */
public class UniqueProjectMembershipList implements Iterable<ProjectMembership> {

    private final ObservableList<ProjectMembership> internalList = FXCollections.observableArrayList();
    private final ObservableList<ProjectMembership> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains the given membership.
     */
    public boolean contains(ProjectMembership membership) {
        requireNonNull(membership);
        return internalList.contains(membership);
    }

    /**
     * Adds a membership to the list.
     * The membership must not already exist in the list.
     */
    public void add(ProjectMembership membership) {
        requireNonNull(membership);
        if (contains(membership)) {
            throw new DuplicateProjectMembershipException();
        }
        internalList.add(membership);
    }

    /**
     * Removes the given membership from the list.
     * The membership must exist in the list.
     */
    public void remove(ProjectMembership membership) {
        requireNonNull(membership);
        if (!internalList.remove(membership)) {
            throw new ProjectMembershipNotFoundException();
        }
    }

    /** Replaces a membership matching the same person and project identifiers. */
    public void set(ProjectMembership membership) {
        requireNonNull(membership);
        int index = internalList.indexOf(membership);
        if (index < 0) {
            throw new ProjectMembershipNotFoundException();
        }
        internalList.set(index, membership);
    }

    /**
     * Replaces the contents of this list with the contents of {@code replacement}.
     */
    public void setMemberships(UniqueProjectMembershipList replacement) {
        requireNonNull(replacement);
        if (replacement != this) {
            internalList.setAll(replacement.internalList);
        }
    }

    /**
     * Replaces the contents of this list with {@code memberships}.
     * {@code memberships} must not contain duplicate memberships.
     */
    public void setMemberships(List<ProjectMembership> memberships) {
        requireAllNonNull(memberships);
        if (!membershipsAreUnique(memberships)) {
            throw new DuplicateProjectMembershipException();
        }
        internalList.setAll(memberships);
    }

    /**
     * Removes all memberships associated with the given person identifier.
     */
    public void removeAllForPerson(PersonId personId) {
        requireNonNull(personId);
        internalList.removeIf(membership -> membership.getPersonId().equals(personId));
    }

    /**
     * Removes all memberships associated with the given project identifier.
     */
    public void removeAllForProject(ProjectId projectId) {
        requireNonNull(projectId);
        internalList.removeIf(membership -> membership.getProjectId().equals(projectId));
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<ProjectMembership> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<ProjectMembership> iterator() {
        return internalUnmodifiableList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof UniqueProjectMembershipList otherMembershipList)) {
            return false;
        }

        return internalList.equals(otherMembershipList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    /**
     * Returns true if {@code memberships} contains only unique person-project pairs.
     */
    private boolean membershipsAreUnique(List<ProjectMembership> memberships) {
        Set<ProjectMembership> seen = new HashSet<>();
        return memberships.stream().allMatch(seen::add);
    }
}
