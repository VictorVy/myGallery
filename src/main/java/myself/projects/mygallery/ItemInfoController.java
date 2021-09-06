package myself.projects.mygallery;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;

public class ItemInfoController //TODO: reduce alarming amount of redundancy with TagManagerController
{
    private ViewItem viewItem;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tagsTab;

    /* ---- GENERAL TAB ---- */

    @FXML
    private Label tagsLbl;

    /* ---- TAGS TAB ---- */

    @FXML
    private ListView<String> tagListView;
    private final ObservableList<String> tags = FXCollections.observableArrayList(); //ListView auto-updates! :)
    private TextFieldListCell<String> clickedCell = new TextFieldListCell<>();

    @FXML
    private TextField searchBar;
    @FXML
    private MenuButton sortBtn;
    @FXML
    private RadioMenuItem ascSort;

    @FXML
    private MenuItem removeMenuItem;
    @FXML
    private Menu selectionMenu;

    private final ImageView sortImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/sortDir.png").toString());

    public void init(ViewItem viewItem)
    {
        this.viewItem = viewItem;

        updateTags();
        generalTabInit();
        tagsTabInit();
    }

    private void generalTabInit()
    {
        ObservableList<String> tagsList = SQLConnector.getItemTags(viewItem, false);

        StringBuilder label = new StringBuilder();

        for(String t : tagsList)
            label.append(label + " ");

        tagsLbl.setText(label.toString().trim());
    }

    private void tagsTabInit()
    {
        ListChangeListener<? super String> listener = (ListChangeListener<String>) c -> updateTags(); //listening for real-time updates
        Main.mainController.tagManagerController.tags.addListener(listener); //adding listener
        tabPane.getScene().getWindow().setOnCloseRequest(e -> Main.mainController.tagManagerController.tags.removeListener(listener)); //removing listener

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

    /* ---- GENERAL TAB ---- */

    @FXML
    public void editTags() { tabPane.getSelectionModel().select(tagsTab); }

    /* ---- TAGS TAB ---- */

    private void updateTags()
    {
        tags.clear();
        tags.addAll(SQLConnector.getItemTags(viewItem, false));
        sortTags();
    }

    private void sortTags() { FXCollections.sort(tags, ascSort.isSelected() ? Comparator.naturalOrder() : Comparator.reverseOrder()); }

    @FXML
    private void attachTags()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/attach-tags-dialog.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Attach tags");
            stage.getIcons().add(new Image(getClass().getResource("/myself/projects/mygallery/images/gallery.png").toString()));

            Scene scene = new Scene(loader.load(), 350, 350);
            scene.getStylesheets().add(getClass().getResource("/myself/projects/mygallery/style.css").toString());

            stage.initModality(Modality.APPLICATION_MODAL); //secret sauce
            stage.setScene(scene);

            AttachTagsDialogController attachTagsDialogController = loader.getController();
            attachTagsDialogController.init(viewItem);

            stage.showAndWait();

            updateTags();
        }
        catch(IOException e) { e.printStackTrace(); }
    }
    @FXML
    private void removeTags()
    {
        if(!tagListView.getSelectionModel().getSelectedItems().isEmpty())
        {
            ObservableList<String> toRemove = tagListView.getSelectionModel().getSelectedItems();
            Alert alert = Dialogs.createRemovalAlert(toRemove, "Confirm Removal", "Remove " + toRemove.size() + " tag(s)?");

            //removing selected items after alerting users
            if(alert.showAndWait().orElse(null) == ButtonType.OK)
            {
                SQLConnector.removeXRef(viewItem.getId(), toRemove);
                updateTags();
            }
        }
    }

    @FXML
    private void toggleSort() { sortTags(); }

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
            selectionMenu.setDisable(true);
            removeMenuItem.setDisable(true);
        }
        else
        {
            selectionMenu.setDisable(false);
            removeMenuItem.setDisable(clickedCell.isEmpty());
        }
    }

    @FXML
    private void selectAll() { tagListView.getSelectionModel().selectAll(); }
    @FXML
    private void clearSelection() { tagListView.getSelectionModel().clearSelection(); }
}
