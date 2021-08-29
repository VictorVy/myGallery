package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class TagManagerController
{
    @FXML
    private ListView<String> tagListView;

    private ObservableList<String> tags = FXCollections.observableArrayList(); //ListView auto-updates! :)

    public void init()
    {
        tags = SQLConnector.getTags();
        tagListView.setItems(tags);
        tagListView.setPlaceholder(new Label("No tags"));
        tagListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void createTag()
    {

    }

    @FXML
    private void deleteTag()
    {
        ObservableList<String> toRemove = tagListView.getSelectionModel().getSelectedItems();

        Alert alert = Alerts.createTagDeletionAlert(toRemove);

        //removing selected tags after alerting users
        if(alert.showAndWait().orElse(null) == ButtonType.OK)
        {

            if(!toRemove.isEmpty())
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
