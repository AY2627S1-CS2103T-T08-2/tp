package seedu.address.model.membership;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.model.person.PersonId;
import seedu.address.model.project.ProjectId;

/**
 * Represents a person's membership in a project.
 * A membership is uniquely identified by its person and project identifiers.
 */
public final class ProjectMembership {

    private final PersonId personId;
    private final ProjectId projectId;
    private final Role role;

    /**
     * Creates a membership between the identified person and project.
     */
    public ProjectMembership(PersonId personId, ProjectId projectId) {
        this(personId, projectId, Role.EMPTY);
    }

    /**
     * Creates a membership with a role describing the person's task allotment.
     */
    public ProjectMembership(PersonId personId, ProjectId projectId, Role role) {
        this.personId = requireNonNull(personId);
        this.projectId = requireNonNull(projectId);
        this.role = requireNonNull(role);
    }

    public PersonId getPersonId() {
        return personId;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    /**
     * Returns the role for this person's membership.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Returns a copy of this membership with {@code newRole}.
     */
    public ProjectMembership withRole(Role newRole) {
        return new ProjectMembership(personId, projectId, newRole);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ProjectMembership otherMembership)) {
            return false;
        }

        return personId.equals(otherMembership.personId)
            && projectId.equals(otherMembership.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, projectId);
    }
}

