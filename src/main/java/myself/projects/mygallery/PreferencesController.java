package myself.projects.mygallery;

import com.sun.javafx.css.StyleManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

public class PreferencesController
{
    private Stage stage;

    @FXML
    private ChoiceBox<String> fontChoiceBox;
    @FXML
    private ChoiceBox<Integer> sizeChoiceBox;

    @FXML
    private Button applyBtn;

    public void init(Stage stage)
    {
        this.stage = stage;

        fontChoiceBox.getItems().addAll("System", "Arial", "Monospace", "Times New Roman", "Comic Sans MS", "Papyrus");
        fontChoiceBox.setOnAction(e -> { if(applyBtn.isDisabled()) applyBtn.setDisable(false); });

        sizeChoiceBox.getItems().addAll(10, 11, 12, 13, 14);
        sizeChoiceBox.setOnAction(e -> { if(applyBtn.isDisabled()) applyBtn.setDisable(false); });

        Main.allScenes.add(stage.getScene().getRoot());
    }

    public void show()
    {
        updateChoiceBoxes();
        applyBtn.setDisable(true);
        stage.show();
    }

    private void updateChoiceBoxes()
    {
        Object[] prefs = SQLConnector.getPrefs();
        fontChoiceBox.setValue((String) prefs[0]);
        sizeChoiceBox.setValue((int) prefs[1]);
    }

    @FXML
    private void cancelBtn() { stage.close(); }
    @FXML
    private void applyBtn()
    {
        applyBtn.setDisable(true);
        SQLConnector.updatePrefs(fontChoiceBox.getValue(), sizeChoiceBox.getValue());
        MiscUtils.updateStyles();
    }
}