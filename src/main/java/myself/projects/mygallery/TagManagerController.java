package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class TagManagerController
{
    @FXML
    private ListView<String> tagListView;

    private ObservableList<String> tags = FXCollections.observableArrayList(); //ListView auto-updates! :)

    private final Stage stage = new Stage();

    public void init()
    {
        tags = SQLConnector.getTags();
        tagListView.setItems(tags);
        tagListView.setPlaceholder(new Label("No tags"));
        tagListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void createTags()
    {
        TextInputDialog dialog = Dialogs.createTextInputDialog();

        Optional<String> input = dialog.showAndWait();

        if(input.isPresent())
        {
            String[] newTags = input.get().split(" ");

            SQLConnector.insertTags(FXCollections.observableArrayList(newTags));
            tagListView.getItems().addAll(newTags);
        }
    }

    @FXML
    private void deleteTags()
    {
        ObservableList<String> toRemove = tagListView.getSelectionModel().getSelectedItems();

        if(!toRemove.isEmpty())
        {
            Alert alert = Dialogs.createRemovalAlert(toRemove);

            //removing selected tags after alerting users
            if(alert.showAndWait().orElse(null) == ButtonType.OK)
            {
                SQLConnector.removeTags(toRemove);
                tags.removeAll(toRemove);
            }
        }
    }
}
