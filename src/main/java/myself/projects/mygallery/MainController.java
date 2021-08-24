package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MainController
{
    //injecting controls
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab galleryTab, detailsTab;

    @FXML
    private FlowPane galleryView;
    private final Label lblEmpty = new Label("Drag and drop or press \"+\"");
    public boolean galleryHover;

    @FXML
    private TableView<ViewItem> detailsView;
    @FXML
    private TableColumn<ViewItem, String> nameColumn, typeColumn, pathColumn, cDateColumn, aDateColumn;

    @FXML
    private ToggleGroup sortToggleGroup;
    public String sortBy = "aDate";
    @FXML
    private MenuButton sortDirBtn;
    @FXML
    private RadioMenuItem ascSortDir;
    public boolean ascending = true;

    private final ImageView sortDirImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/sortDir.png").toString());

    public void init()
    {
        //preparing table view
        detailsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        detailsView.setPlaceholder(new Label("Drag files or press Add"));
        detailsView.setOnMouseClicked(e -> detailsClicked(detailsView.getSelectionModel().getSelectedItem(), e.getClickCount()));
        //preparing columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        cDateColumn.setCellValueFactory(new PropertyValueFactory<>("cDate"));
        aDateColumn.setCellValueFactory(new PropertyValueFactory<>("aDate"));

        //better than onSelectionChanged
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            //updates/synchronizes selections accordingly
            if(newValue.equals(galleryTab) && !detailsView.getSelectionModel().getSelectedItems().isEmpty())
                SelectionHandler.findAndSelect(detailsView.getSelectionModel().getSelectedItems());
            else if(newValue.equals(detailsTab) && SelectionHandler.getSelected().size() > 0)
            {
                detailsView.getSelectionModel().clearSelection();

                for(ViewItem vi : SelectionHandler.getSelected())
                    detailsView.getSelectionModel().select(ViewItem.indexOf(getViewItems(), vi));
            }
        });

        //graphics
        sortDirImg.setPreserveRatio(true);
        sortDirImg.setFitHeight(16); //hmm...
        sortDirBtn.setGraphic(sortDirImg);

        SQLConnector.initialize();
        MediaUtils.initialize();
        SelectionHandler.initialize();
        updateViews();
    }

    @FXML
    private void addFiles()
    {
        //choosing files
        FileChooser fc = new FileChooser();
        fc.setTitle("Add Files");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Files", "*.png", "*.jpg", "*.bmp", "*.gif", "*.mp4", "*.m4v", "*.mp3", "*.wav", "*.aif", "*.aiff"));
        List<File> files = fc.showOpenMultipleDialog(Main.mainScene.getWindow());

        if(files != null)
        {
            //inserts items into the db
            SQLConnector.insert(MediaUtils.filesToViewItems(files));
            //generating thumbnails
            MediaUtils.createThumbs(MediaUtils.filesToViewItems(files), 150);

            updateViews();
        }
    }

    @FXML
    private void removeFiles()
    {
        ObservableList<ViewItem> selectedItems = FXCollections.observableArrayList();

        if(galleryTab.isSelected())
            selectedItems = SelectionHandler.getSelected();
        else if(detailsTab.isSelected())
            selectedItems = detailsView.getSelectionModel().getSelectedItems();

        remove(selectedItems);
    }

    @FXML
    private void removeAll()
    {
        remove(SQLConnector.getDBItems());
    }
    private void remove(ObservableList<ViewItem> items)
    {
        if(items.size() > 0)
        {
            Alert alert = Alerts.createRemovalAlert(items);

            //removing selected items after alerting users
            if(alert.showAndWait().orElse(null) == ButtonType.OK)
            {
                SQLConnector.remove(items);
                MediaUtils.removeThumbs(items);
                updateViews();
            }
        }
    }
    @FXML
    private void syncFiles()
    {
        if(Objects.requireNonNull(SQLConnector.getDBItems()).size() > 0)
        {
            //checking if items in the db exist; if not, remove them
            ObservableList<ViewItem> viewItems = SQLConnector.getDBItems();
            ObservableList<ViewItem> toRemove = FXCollections.observableArrayList();

            for(ViewItem vi : viewItems)
                if(!(new File(vi.getPath()).exists())) toRemove.add(vi);

            if(toRemove.size() > 0)
            {
                SQLConnector.remove(toRemove);
                MediaUtils.removeThumbs(toRemove);
            }

            updateViews();
        }
    }

    @FXML
    private void selectAll()
    {
        SelectionHandler.selectAll();
        detailsView.getSelectionModel().selectAll();
    }

    @FXML
    private void clearSelection()
    {
        SelectionHandler.clearSelection();
        detailsView.getSelectionModel().clearSelection();
    }
    @FXML
    private void sortToggle()
    {
        RadioMenuItem selected = (RadioMenuItem) sortToggleGroup.getSelectedToggle();
        sortBy = selected.getId().substring(0, selected.getId().indexOf("Sort"));
        updateViews();
    }

    @FXML
    private void sortDirToggle()
    {
        ascending = ascSortDir.isSelected();
        updateViews();
    }

    @FXML
    private void galleryClicked()
    {
        if(!galleryHover)
            SelectionHandler.clearSelection();
    }
    public void detailsClicked(ViewItem viewItem, int clickCount)
    {
        if(clickCount == 2)
            showItem(viewItem);
    }

    //handling dragging files into view

    @FXML
    private void dragOverView(DragEvent event)
    {
        if(event.getDragboard().hasFiles())
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
    }
    @FXML
    private void dragDropView(DragEvent event)
    {
        List<File> dragFiles = event.getDragboard().getFiles();

        if(MediaUtils.wrongFiles(dragFiles).size() == 0)
        {
            SQLConnector.insert(MediaUtils.filesToViewItems(dragFiles));
            MediaUtils.createThumbs(MediaUtils.filesToViewItems(dragFiles), 150);

            updateViews();
        }
        else
            Alerts.createDragAlert(MediaUtils.wrongFiles(dragFiles)).showAndWait();
    }

    //updates the view

    public void updateViews()
    {
        updateGalleryView();
        updateDetailsView();
    }

    public void updateGalleryView()
    {
        ObservableList<ViewItem> viewItems = SQLConnector.getDBItems();
        galleryView.getChildren().clear();
        galleryView.setAlignment(Pos.BASELINE_LEFT);

        if(viewItems != null && !viewItems.isEmpty())
        {
            for(ViewItem vi : viewItems)
                galleryView.getChildren().add(new ViewItemWrapper(vi));
        }
        else
        {
            galleryView.getChildren().add(lblEmpty);
            galleryView.setAlignment(Pos.CENTER);
        }
    }
    public ObservableList<ViewItem> getViewItems()
    {
        ObservableList<ViewItem> items = FXCollections.observableArrayList();

        for(int i = 0; i < galleryView.getChildren().size(); i++)
            items.add(((ViewItemWrapper) galleryView.getChildren().get(i)).getViewItem());

        return items;
    }

    private void updateDetailsView() { detailsView.setItems(SQLConnector.getDBItems()); }

    public void showItem(ViewItem viewItem)
    {
        try //necessary?
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/view-window.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(viewItem.getName() + "." + viewItem.getType());
            stage.getIcons().add(new Image(getClass().getResource("/myself/projects/mygallery/images/gallery.png").toString()));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/myself/projects/mygallery/style.css").toString());

            stage.setScene(scene);

            ViewWindowController viewWindowController = loader.getController();
            viewWindowController.init(viewItem, stage);

            stage.show();
        }
        catch(IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void close() { Main.close(); }
}