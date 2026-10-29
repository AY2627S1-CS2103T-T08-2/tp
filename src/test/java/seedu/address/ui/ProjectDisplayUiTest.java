package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.project.Project;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.ProjectBuilder;

/**
 * Regression coverage for filtered-empty project views and narrow project cards.
 */
public class ProjectDisplayUiTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        JavaFxTestUtil.startToolkit();
    }

    @Test
    public void projectList_emptyAndFilteredEmpty_offerRecoveryWithoutClaimingNoData() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            Model model = new ModelManager();
            ProjectListPanel panel = new ProjectListPanel(model.getFilteredProjectList());
            ListView<?> list = (ListView<?>) panel
                .getRoot()
                .lookup("#projectListView");
            Label placeholder = (Label) list.getPlaceholder();
            assertTrue(placeholder
                .getText()
                .contains("listprojects"));
            assertTrue(placeholder
                .getText()
                .contains("addproject"));
            assertFalse(placeholder
                .getText()
                .contains("No projects yet"));

            Project project = new ProjectBuilder()
                .withName("Alpha")
                .build();
            model.addProject(project);
            model.updateFilteredProjectList(value -> false);
            assertTrue(list
                .getItems()
                .isEmpty());
            assertEquals(1, model
                .getAddressBook()
                .getProjectList()
                .size());
            assertFalse(placeholder
                .getText()
                .contains("No projects yet"));
            model.updateFilteredProjectList(Model.PREDICATE_SHOW_ALL_PROJECTS);
            assertEquals(1, list
                .getItems()
                .size());
            return null;
        });
        Platform.runLater(check);
        check.get(30, TimeUnit.SECONDS);
    }

    @Test
    public void projectCard_longName_wrapsAtNarrowWidthAndProvidesFullTooltip() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            String projectName = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWX";
            Project project = new ProjectBuilder()
                .withName(projectName)
                .build();
            Region card = new ProjectCard(project, 1).getRoot();
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(card, 260, 450));
                stage.show();
                card.applyCss();
                card.layout();
                Label name = (Label) card.lookup("#name");
                assertTrue(name.isWrapText());
                assertEquals(projectName, name.getText());
                assertEquals(projectName, name
                    .getTooltip()
                    .getText());
                assertTrue(name.getHeight() > name
                        .getFont()
                        .getSize() * 1.5,
                    "The long name should occupy multiple lines at a narrow width");
                return null;
            } finally {
                stage.close();
            }
        });
        Platform.runLater(check);
        check.get(30, TimeUnit.SECONDS);
    }
}
