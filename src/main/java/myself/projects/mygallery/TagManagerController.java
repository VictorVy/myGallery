package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.util.Comparator;
import java.util.Optional;

public class TagManagerController
{
    private Stage stage;

    @FXML
    private ListView<String> tagListView;
    public final ObservableList<String> tags = FXCollections.observableArrayList(); //ListView auto-updates! :)
    private TextFieldListCell<String> clickedCell;

    @FXML
    private TextField searchBar;
    @FXML
    private MenuButton sortBtn;
    @FXML
    private RadioMenuItem ascSort;

    @FXML
    private MenuItem renameMenuItem, deleteMenuItem;
    @FXML
    private Menu selectionMenu;

    private final ImageView sortImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/sortDir.png")));

    public void init(Stage stage)
    {
        this.stage = stage;
        updateTags();
        //prepare list view
        tagListView.setPlaceholder(new Label("No tags"));
        tagListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tagListView.setCellFactory(lv ->
        {
            TextFieldListCell<String> cell = new TextFieldListCell<>(new DefaultStringConverter()); //editable cell
            cell.setOnMouseClicked(e -> clickedCell = cell);
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

        Main.allScenes.add(stage.getScene().getRoot());
    }

    void show() { stage.show(); }

    private void updateTags()
    {
        tags.clear();
        tags.addAll(SQLConnector.getTags());
        sortTags();
    }

    private void sortTags() { FXCollections.sort(tags, ascSort.isSelected() ? Comparator.naturalOrder() : Comparator.reverseOrder()); }

    @FXML
    private void tagListMouseClicked()
    {
        if(!tagListView.getItems().isEmpty() && clickedCell.isEmpty())
            tagListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void tagListContextMenuRequested()
    {
        if(tagListView.getItems().isEmpty())
        {
            renameMenuItem.setDisable(true);
            selectionMenu.setDisable(true);
            deleteMenuItem.setDisable(true);
        }
        else
        {
            renameMenuItem.setDisable(clickedCell.isEmpty() || tagListView.getSelectionModel().getSelectedItems().size() > 1);
            selectionMenu.setDisable(false);
            deleteMenuItem.setDisable(clickedCell.isEmpty());
        }
    }

    @FXML
    private void createTags()
    {
        TextInputDialog dialog = Dialogs.createTextInputDialog();

        Optional<String> input = dialog.showAndWait();

        if(input.isPresent())
        {
            String[] newTags = input.get().split("\\s+"); //swapped stream for regex :)

            SQLConnector.insertTags(FXCollections.observableArrayList(newTags));

            updateTags();
            //select new tags
            for(String t : newTags) tagListView.getSelectionModel().select(t);
        }
    }

    @FXML
    private void deleteTags()
    {
        ObservableList<String> toRemove = tagListView.getSelectionModel().getSelectedItems();

        if(!toRemove.isEmpty())
        {
            Alert alert = Dialogs.createRemovalAlert(toRemove, "Confirm Deletion", "Delete " + toRemove.size() + " tag(s)");

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

        if(!tagListView.getItems().contains(newName))
        {
            SQLConnector.renameTag(oldName, newName);
            updateTags();
            tagListView.getSelectionModel().select(newName);
        }
        else
        {
            e.consume();
            tagListView.getSelectionModel().select(oldName);
        }
    }
    @FXML
    private void renameMenuItem() { clickedCell.startEdit(); }

    @FXML
    private void toggleSort() { sortTags(); }

    @FXML
    private void selectAll() { tagListView.getSelectionModel().selectAll(); }
    @FXML
    private void clearSelection() { tagListView.getSelectionModel().clearSelection(); }

    @FXML
    private void keyPressed(KeyEvent e)
    {
        KeyCode code = e.getCode();

        if(!searchBar.isFocused() && (code.equals(KeyCode.DELETE) || code.equals(KeyCode.BACK_SPACE))) deleteTags();
    }
}