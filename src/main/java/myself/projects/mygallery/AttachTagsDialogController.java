package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Comparator;

public class AttachTagsDialogController
{
    private ViewItem viewItem;
    private Stage stage;

    @FXML
    private BorderPane borderPane;

    @FXML
    private ListView<String> tagListView;
    private final ObservableList<String> tags = FXCollections.observableArrayList(); //ListView auto-updates! :)
    private TextFieldListCell<String> clickedCell;

    @FXML
    private TextField searchBar;
    @FXML
    private MenuButton sortBtn;
    @FXML
    private RadioMenuItem ascSort, showAttachedSort;

    @FXML
    private Menu selectionMenu;

    private final ImageView sortImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/sortDir.png").toString());

    public void init(ViewItem viewItem)
    {
        this.viewItem = viewItem;
        stage = (Stage) borderPane.getScene().getWindow();

        ListChangeListener<? super String> listener = (ListChangeListener<String>) c -> updateTags(); //listening for real-time updates
        Main.mainController.tagManagerController.tags.addListener(listener); //adding listener
        stage.setOnCloseRequest(e -> Main.mainController.tagManagerController.tags.removeListener(listener)); //removing listener

        updateTags();
        //prepare list view
        tagListView.setPlaceholder(new Label("No tags"));
        tagListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tagListView.setCellFactory(lv ->
        {
            TextFieldListCell<String> cell = new TextFieldListCell<>();
            cell.setEditable(false);
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
    }

    private void updateTags()
    {
        tags.clear();
        tags.addAll(showAttachedSort.isSelected() ? SQLConnector.getTags() : SQLConnector.getItemTags(viewItem, true));
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
            selectionMenu.setDisable(true);
        else
            selectionMenu.setDisable(false);
    }

    @FXML
    private void toggleSort() { sortTags(); }
    @FXML
    private void showAttachedToggle() { updateTags(); }

    @FXML
    private void selectAll() { tagListView.getSelectionModel().selectAll(); }
    @FXML
    private void clearSelection() { tagListView.getSelectionModel().clearSelection(); }

    @FXML
    private void attachConfirm()
    {
        if(!tagListView.getSelectionModel().getSelectedItems().isEmpty())
            SQLConnector.insertXRef(viewItem.getId(), tagListView.getSelectionModel().getSelectedItems());

        stage.close();
    }
    @FXML
    private void attachCancel() { stage.close(); }

    @FXML
    private void manageTags() { Main.mainController.tagManagerController.show(); }
}
