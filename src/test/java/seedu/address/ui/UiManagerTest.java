package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class UiManagerTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        assumeTrue(!isLinux || System.getenv("DISPLAY") != null || System.getenv("WAYLAND_DISPLAY") != null,
                "JavaFX UI tests require a graphical display.");
        FutureTask<Void> startup = new FutureTask<>(() -> Platform.setImplicitExit(false), null);
        Platform.startup(startup);
        startup.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void start_savingDisabled_showsRecoveryDialogAndPersistentWarning() throws Exception {
        runOnFxThread(() -> {
            RecordingUiManager ui = createUiManager(true);
            Stage stage = new Stage();
            try {
                ui.start(stage);
                assertEquals(AlertType.ERROR, ui.alertType);
                assertTrue(ui.alertContent.contains(testFolder.resolve("addressbook.json")
                        .toAbsolutePath().normalize().toString()));
                assertTrue(ui.alertContent.contains("Your original file has been preserved."));
                assertTrue(ui.alertContent.contains("repair it or restore a valid backup and restart."));
                Label warning = (Label) stage.getScene().lookup("#savingDisabledStatus");
                assertTrue(warning.isVisible());
                assertTrue(warning.isManaged());
                assertEquals("Saving disabled — session changes will be lost", warning.getText());
                Label path = (Label) stage.getScene().lookup("#saveLocationStatus");
                assertEquals(testFolder.resolve("addressbook.json").toAbsolutePath().normalize().toString(),
                        path.getText());
                assertEquals(path.getText(), path.getTooltip().getText());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void start_savingEnabled_showsNoRecoveryDialogOrWarning() throws Exception {
        runOnFxThread(() -> {
            RecordingUiManager ui = createUiManager(false);
            Stage stage = new Stage();
            try {
                ui.start(stage);
                assertNull(ui.alertType);
                Label warning = (Label) stage.getScene().lookup("#savingDisabledStatus");
                assertFalse(warning.isVisible());
                assertFalse(warning.isManaged());
            } finally {
                stage.close();
            }
        });
    }

    private RecordingUiManager createUiManager(boolean savingDisabled) {
        Path dataFilePath = testFolder.resolve("addressbook.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFilePath),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(new ModelManager(), storage);
        return new RecordingUiManager(logic, dataFilePath, savingDisabled);
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    /**
     * Records recovery alerts without opening a modal dialog during automated tests.
     */
    private static class RecordingUiManager extends UiManager {
        private AlertType alertType;
        private String alertContent;

        RecordingUiManager(Logic logic, Path dataFilePath, boolean savingDisabled) {
            super(logic, dataFilePath, savingDisabled);
        }

        @Override
        void showAlertDialogAndWait(AlertType type, String title, String headerText, String contentText) {
            alertType = type;
            alertContent = contentText;
        }
    }
}
