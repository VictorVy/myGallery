package myself.projects.mygallery;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private Button applyBtn, cancelBtn;

    public void init(Stage stage)
    {
        this.stage = stage;

        fontChoiceBox.getItems().addAll("System", "Arial", "Monospace", "Times New Roman", "Comic Sans MS", "Papyrus");
        fontChoiceBox.setValue("System");
        fontChoiceBox.setOnAction(e -> { if(applyBtn.isDisabled()) applyBtn.setDisable(false); });

        sizeChoiceBox.getItems().addAll(10, 11, 12, 13, 14);
        sizeChoiceBox.setValue(12);
        sizeChoiceBox.setOnAction(e -> { if(applyBtn.isDisabled()) applyBtn.setDisable(false); });

        Main.allScenes.add(stage.getScene().getRoot());
    }

    public void show() { stage.show(); }

    @FXML
    private void cancelBtn() { stage.close(); }
    @FXML
    private void applyBtn()
    {
        applyBtn.setDisable(true);
        updateStylesheets();
//        SQLConnector.updatePrefs();
    }

    private void updateStylesheets()
    {
        for(Parent p : Main.allScenes)
            p.setStyle("-fx-font-family: '" + fontChoiceBox.getValue() + "';" +
                       "-fx-font-size: " + sizeChoiceBox.getValue() + ";");
    }
}