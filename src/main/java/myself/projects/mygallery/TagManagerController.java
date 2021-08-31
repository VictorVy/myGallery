package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.Optional;

public class TagManagerController
{
    @FXML
    private ListView<String> tagListView;
    private ObservableList<String> tags = FXCollections.observableArrayList(); //ListView auto-updates! :)

    @FXML
    private TextField searchBar;
    @FXML
    private MenuButton sortBtn;
    @FXML
    private RadioMenuItem ascSort;

    private final ImageView sortImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/sortDir.png").toString());

    public void init()
    {
        //get and sort tags
        tags = SQLConnector.getTags();
        sortTags();
        //prepare list view
        tagListView.setPlaceholder(new Label("No tags"));
        tagListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        tagListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
//        {
//            @Override
//            public ListCell<String> call(ListView<String> param)
//            {
//                ListCell<String> cell = new ListCell<String>()
//                {
//                    @Override
//                    protected void updateItem(String s, boolean empty)
//                    {
//                        super.updateItem(s, empty);
//                    }
//                };
//                return cell;
//            }
//        });
        //set up search bar
        FilteredList<String> filteredTags = new FilteredList<>(tags);
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> filteredTags.setPredicate(s -> s.contains(newValue)));
        //sort out sort button (redundant code!!)
        sortImg.setPreserveRatio(true);
        sortImg.setFitHeight(16); //hmm...
        sortBtn.setGraphic(sortImg);

        tagListView.setItems(filteredTags);
    }

    private void sortTags() { FXCollections.sort(tags, ascSort.isSelected() ? Comparator.naturalOrder() : Comparator.reverseOrder()); }

    @FXML
    private void createTags()
    {
        TextInputDialog dialog = Dialogs.createTextInputDialog();

        Optional<String> input = dialog.showAndWait();

        if(input.isPresent())
        {
            String[] newTags = input.get().split(" ");

            SQLConnector.insertTags(FXCollections.observableArrayList(newTags));

            tags.addAll(newTags);
            sortTags();
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

    @FXML
    private void toggleSort() { sortTags(); }
}
