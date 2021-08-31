package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.converter.DefaultStringConverter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class TagManagerController
{
    @FXML
    private ListView<String> tagListView;
    private ObservableList<String> tags = FXCollections.observableArrayList(); //ListView auto-updates! :)
    private TextFieldListCell<String> clickedCell;

    @FXML
    private TextField searchBar;
    @FXML
    private MenuButton sortBtn;
    @FXML
    private RadioMenuItem ascSort;

    @FXML
    private ContextMenu tagListContextMenu;

    private final ImageView sortImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/sortDir.png").toString());

    public void init()
    {
        updateTags();
        //prepare list view
        tagListView.setPlaceholder(new Label("No tags"));
        tagListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tagListView.setCellFactory(lv ->
        {
            TextFieldListCell<String> cell = new TextFieldListCell<>(new DefaultStringConverter()); //editable cell

            cell.setOnMouseClicked(e ->
            {
                clickedCell = cell;
                tagListClicked(e);
            });

            return cell;
        });
        //set up search bar
        FilteredList<String> filteredTags = new FilteredList<>(tags);
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> filteredTags.setPredicate(s -> s.contains(newValue)));
        //sort out sort button (redundant code!!)
        sortImg.setPreserveRatio(true);
        sortImg.setFitHeight(16); //hmm...
        sortBtn.setGraphic(sortImg);

        tagListView.setItems(filteredTags);
    }

    private void updateTags()
    {
        tags.clear();
        tags.addAll(SQLConnector.getTags());
        sortTags();
    }

    private void sortTags() { FXCollections.sort(tags, ascSort.isSelected() ? Comparator.naturalOrder() : Comparator.reverseOrder()); }

    private void tagListClicked(MouseEvent e)
    {
        if(clickedCell.isEmpty())
            tagListView.getSelectionModel().clearSelection();

        if(e.getButton().equals(MouseButton.SECONDARY))
        {
            tagListContextMenu.getItems().get(1).setDisable(clickedCell.isEmpty() || tagListView.getSelectionModel().getSelectedItems().size() > 1);
            tagListContextMenu.getItems().get(2).setDisable(clickedCell.isEmpty());
        }
    }

    @FXML
    private void createTags()
    {
        TextInputDialog dialog = Dialogs.createTextInputDialog();

        Optional<String> input = dialog.showAndWait();

        if(input.isPresent())
        {
            //a regexp would probably work here, but streams are fun :)
            SQLConnector.insertTags(
                    FXCollections.observableArrayList(
                            Arrays.stream(input.get().split(" ")).map(String::trim).toArray(String[]::new)));

            updateTags();
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
                updateTags();
            }
        }
    }

    @FXML
    private void renameTag(ListView.EditEvent e)
    {
        String oldName = tagListView.getItems().get(e.getIndex()), newName = e.getNewValue().toString().trim().replace(' ', '_');
        SQLConnector.renameTag(oldName, newName);
        updateTags();
        tagListView.getSelectionModel().select(newName);
    }
    @FXML
    private void renameMenuItem() { clickedCell.startEdit(); }

    @FXML
    private void toggleSort() { sortTags(); }
}
