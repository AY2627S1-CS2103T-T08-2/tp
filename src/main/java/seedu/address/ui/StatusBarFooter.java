package seedu.address.ui;

import java.nio.file.Path;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;

/**
 * A UI for the status bar that is displayed at the footer of the application.
 */
public class StatusBarFooter extends UiPart<Region> {

    private static final String FXML = "StatusBarFooter.fxml";

    @FXML
    private Label saveLocationStatus;

    @FXML
    private Label savingDisabledStatus;

    /**
     * Creates a {@code StatusBarFooter} with the given save location and session saving restriction.
     */
    public StatusBarFooter(Path saveLocation, boolean savingDisabled) {
        super(FXML);
        String absolutePath = saveLocation.toAbsolutePath().normalize().toString();
        saveLocationStatus.setText(absolutePath);
        saveLocationStatus.setTooltip(new Tooltip(absolutePath));
        savingDisabledStatus.setVisible(savingDisabled);
        savingDisabledStatus.setManaged(savingDisabled);
    }

}
