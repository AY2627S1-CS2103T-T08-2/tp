package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.project.Description;
import seedu.address.model.project.Project;

/**
 * A UI component that displays information about a {@code Project}.
 */
public class ProjectCard extends UiPart<Region> {

    private static final String FXML = "ProjectListCard.fxml";

    public final Project project;

    @FXML
    private HBox cardPane;
    @FXML
    private Label displayedIndex;
    @FXML
    private Label name;
    @FXML
    private Label projectId;
    @FXML
    private Label deadline;
    @FXML
    private Label description;

    /**
     * Creates a {@code ProjectCard} with the given project and displayed index.
     */
    public ProjectCard(Project project, int displayedIndex) {
        super(FXML);
        this.project = project;
        this.displayedIndex.setText(displayedIndex + ". ");
        name.setText(project.getName().toString());
        projectId.setText("ID: " + project.getProjectId());
        deadline.setText("Deadline: " + project.getDeadline());

        String descriptionText = project.getDescription().equals(Description.EMPTY)
                ? "No description provided"
                : project.getDescription().toString();
        description.setText("Description: " + descriptionText);
    }
}
