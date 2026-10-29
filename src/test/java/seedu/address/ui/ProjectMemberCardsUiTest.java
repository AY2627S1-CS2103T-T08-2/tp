package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.membership.ProjectMembership;
import seedu.address.model.project.Project;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.ProjectBuilder;

/**
 * Exercises real JavaFX cards and command-driven tab selection when a display is available.
 */
public class ProjectMemberCardsUiTest {

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void startJavaFx() throws Exception {
        JavaFxTestUtil.startToolkit();
    }

    @Test
    public void memberCommands_displayCardsSwitchTabsAndRestoreContacts() throws Exception {
        CompletableFuture<Void> finished = new CompletableFuture<>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                Model model = new ModelManager();
                model.addPerson(ALICE);
                model.addPerson(BENSON);
                Project project = new ProjectBuilder()
                    .withName("CS2103T Team Project")
                    .build();
                Project emptyProject = new ProjectBuilder()
                    .withName("Orbital")
                    .build();
                model.addProject(project);
                model.addProject(emptyProject);
                model.addProjectMembership(new ProjectMembership(BENSON.getPersonId(), project.getProjectId()));
                Path dataPath = testFolder.resolve("addressbook.json");
                StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
                MainWindow window = new MainWindow(stage, new LogicManager(model, storage), dataPath, false);
                window.fillInnerParts();
                stage.setWidth(1000);
                stage.setHeight(760);
                stage.show();
                stage
                    .getScene()
                    .getRoot()
                    .applyCss();
                stage
                    .getScene()
                    .getRoot()
                    .layout();
                TabPane tabs = (TabPane) stage
                    .getScene()
                    .lookup("#listTabs");
                TextField input = (TextField) stage
                    .getScene()
                    .lookup("#commandTextField");

                enterCommand(input, "listprojects");
                assertEquals("Projects", tabs
                    .getSelectionModel()
                    .getSelectedItem()
                    .getText());
                enterCommand(input, "addproject n/UI Demo d/2026-11-13");
                assertEquals("Projects", tabs
                    .getSelectionModel()
                    .getSelectedItem()
                    .getText());
                enterCommand(input, "listmembers pr/CS2103T Team Project");
                assertEquals("Contacts", tabs
                    .getSelectionModel()
                    .getSelectedItem()
                    .getText());
                assertEquals(List.of(BENSON), model.getFilteredPersonList());
                stage
                    .getScene()
                    .getRoot()
                    .applyCss();
                stage
                    .getScene()
                    .getRoot()
                    .layout();
                assertFalse(stage
                    .getScene()
                    .getRoot()
                    .lookupAll("#cardPane")
                    .isEmpty());
                savePreview(stage);

                enterCommand(input, "listmembers pr/Orbital");
                assertTrue(model
                    .getFilteredPersonList()
                    .isEmpty());
                ListView<?> contacts = (ListView<?>) stage
                    .getScene()
                    .lookup("#personListView");
                assertEquals(0, contacts
                    .getItems()
                    .size());
                enterCommand(input, "list");
                assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
                assertEquals("Contacts", tabs
                    .getSelectionModel()
                    .getSelectedItem()
                    .getText());
                finished.complete(null);
            } catch (Throwable error) {
                finished.completeExceptionally(error);
            } finally {
                stage.close();
            }
        });
        finished.get(30, TimeUnit.SECONDS);
    }

    /**
     * Sends a command through the same action handler used by the Enter key.
     */
    private static void enterCommand(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
        assertEquals("", input.getText());
    }

    /**
     * Saves the rendered scene for local visual inspection alongside test reports.
     */
    private static void savePreview(Stage stage) throws Exception {
        WritableImage snapshot = stage
            .getScene()
            .snapshot(null);
        BufferedImage image = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, snapshot
                    .getPixelReader()
                    .getArgb(x, y));
            }
        }
        Path output = Path.of("build", "reports", "ui", "project-members.png");
        Files.createDirectories(output.getParent());
        ImageIO.write(image, "png", output.toFile());
    }
}
