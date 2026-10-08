package seedu.address.model.project;

import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ProjectBuilder;

public class ProjectTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Project project = new ProjectBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }
}
