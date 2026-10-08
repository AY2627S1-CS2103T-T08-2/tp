package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalProjects.PROJ0;
import static seedu.address.testutil.TypicalProjects.PROJ1;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ProjectBuilder;

public class ProjectTest {

    @Test
    public void equals() {
        // same values -> returns true
        Project proj0Copy = new ProjectBuilder(PROJ0).build();
        assertTrue(PROJ0.equals(proj0Copy));

        // same object -> returns true
        assertTrue(PROJ0.equals(PROJ0));

        // null -> returns false
        assertFalse(PROJ0.equals(null));

        // different type -> returns false
        assertFalse(PROJ0.equals(5));

        // different Project -> returns false
        assertFalse(PROJ0.equals(PROJ1));
    }
}
