package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

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

    public void show() { stage.showAndWait(); }

    @FXML
    private void createTag()
    {

    }

    @FXML
    private void deleteTag()
    {
        ObservableList<String> toRemove = tagListView.getSelectionModel().getSelectedItems();

        if(!toRemove.isEmpty())
        {
            Alert alert = Alerts.createRemovalAlert((String[]) toRemove.toArray()); //remember to TEST REMOVAL

            //removing selected tags after alerting users
            if(alert.showAndWait().orElse(null) == ButtonType.OK)
            {
                SQLConnector.removeTags(toRemove);
                tags.removeAll(toRemove);
            }
        }
    }

//    @FXML
//    private void addTestTags()
//    {
//        ObservableList<String> tags = FXCollections.observableArrayList();
//
//        tags.add("test1");
//        tags.add("test2");
//        tags.add("test3");
//
//        SQLConnector.insertTags(tags);
//    }
}
