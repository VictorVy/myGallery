package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainController implements Initializable
{
    //injecting controls
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab galleryTab, detailsTab;

    @FXML
    private FlowPane galleryView;
    private final Label lblEmpty = new Label("Drag and drop or press \"+\"");
    private boolean galleryHover;

    @FXML
    private TableView<ViewItem> detailsView;
    @FXML
    private TableColumn<ViewItem, String> nameColumn, typeColumn, pathColumn, cDateColumn, aDateColumn;

    @FXML
    private ToggleGroup sortToggleGroup;
    public static String sortBy = "aDate";
    @FXML
    private MenuButton sortDirBtn;
    @FXML
    private RadioMenuItem ascSortDir;
    public static boolean ascending = true;

    ImageView sortDirImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/sortDir.png").toString());

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        //preparing table view
        detailsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        detailsView.setPlaceholder(new Label("Drag files or press Add"));
        detailsView.setOnMouseClicked(e -> SelectionHandler.detailsClicked(detailsView.getSelectionModel().getSelectedItem(), e.getClickCount()));
        //preparing columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        cDateColumn.setCellValueFactory(new PropertyValueFactory<>("cDate"));
        aDateColumn.setCellValueFactory(new PropertyValueFactory<>("aDate"));


        //graphics
        sortDirImg.setPreserveRatio(true);
        sortDirImg.setFitHeight(16); //hmm...
        sortDirBtn.setGraphic(sortDirImg);

        SQLConnector.initialize();
        MediaUtils.initialize();
        updateView();
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

            updateView();
        }
    }

    @FXML
    private void removeFiles()
    {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        ObservableList<ViewItem> selectedItems = FXCollections.observableArrayList();

        if(tab.equals(galleryTab))
            selectedItems = SelectionHandler.getSelected();
        else if(tab.equals(detailsTab))
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
                updateView();
            }
        }
    }

    @FXML
    private void btnRefresh()
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

            updateView();
        }
    }

    @FXML
    private void btnClear()
    {
        SelectionHandler.deselectAll();
        detailsView.getSelectionModel().clearSelection();
        updateGalleryView();
    }

    @FXML
    private void selectAll()
    {
        detailsView.getSelectionModel().selectAll();
        updateView();
    }
    @FXML
    private void clearSelection()
    {
        SelectionHandler.deselectAll();
        updateView();
    }

    @FXML
    private void sortToggle()
    {
        RadioMenuItem selected = (RadioMenuItem) sortToggleGroup.getSelectedToggle();
        sortBy = selected.getId().substring(0, selected.getId().indexOf("Sort"));
        updateView();
    }

    @FXML
    private void sortDirToggle()
    {
        ascending = ascSortDir.isSelected();
        updateView();
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

            updateView();
        }
        else
            Alerts.createDragAlert(MediaUtils.wrongFiles(dragFiles)).showAndWait();
    }

    @FXML
    private void galleryScrollMouseReleased()
    {
        if(!galleryHover)
        {
            SelectionHandler.deselectAll();
            updateGalleryView();
        }
    }

    //updates the view
    public void updateView()
    {
        if(galleryTab.isSelected())
        {
            //sync selection between views
            try { SelectionHandler.setSelected(detailsView.getSelectionModel().getSelectedItems()); } //detailsView not init yet
            catch (Exception e) { System.out.println("e.printStackTrace(lol);"); }

            updateGalleryView();
        }
        else if(detailsTab.isSelected())
        {
            ObservableList<ViewItem> viewItems = SQLConnector.getDBItems();
            detailsView.setItems(viewItems);

            //sync selection between views
            if(SelectionHandler.getSelected().size() > 0)
            {
                detailsView.getSelectionModel().clearSelection();
                detailsView.getSelectionModel().selectRange(ViewItem.indexOf(Objects.requireNonNull(viewItems), SelectionHandler.getSelected().get(0)),
                                                            ViewItem.indexOf(viewItems, SelectionHandler.getSelected().get(SelectionHandler.getSelected().size() - 1)) + 1);
            }
        }
    }

    public void galleryViewClicked() { updateGalleryView(); }

    public void updateGalleryView()
    {
        ObservableList<ViewItem> viewItems = SQLConnector.getDBItems();
        galleryView.getChildren().clear();
        galleryView.setAlignment(Pos.BASELINE_LEFT);

        if(viewItems != null && !viewItems.isEmpty())
        {
            for(ViewItem vi : viewItems)
            {
                ImageView imageView = new ImageView("file:" + vi.getThumb());
                //creates hover effect
                imageView.setOnMouseEntered(e -> imageView.setEffect(MediaUtils.hoverEffect()));
                imageView.setOnMouseExited(e -> imageView.setEffect(null));

                Pane imageViewWrapper = new BorderPane(imageView);
                imageViewWrapper.setPadding(new Insets(5));
                imageViewWrapper.setOnMouseClicked(e -> SelectionHandler.viewItemClicked(vi, e.isShiftDown(), e.isControlDown(), e.getButton(), e.getClickCount()));
                imageViewWrapper.setOnMouseEntered(e -> galleryHover = true);
                imageViewWrapper.setOnMouseExited(e -> galleryHover = false);

                if(SelectionHandler.isSelected(vi))
                    imageViewWrapper.getStyleClass().add("image-view-selected");

                galleryView.getChildren().add(imageViewWrapper);
            }
        }
        else
        {
            galleryView.getChildren().add(lblEmpty);
            galleryView.setAlignment(Pos.CENTER);
        }
    }

    public static void showItem(ViewItem viewItem)
    {
        try //necessary?
        {
            FXMLLoader loader = new FXMLLoader(SelectionHandler.class.getResource("/myself/projects/mygallery/view-window.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(viewItem.getName() + "." + viewItem.getType());
            stage.getIcons().add(new Image(SelectionHandler.class.getResource("/myself/projects/mygallery/images/gallery.png").toString()));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(SelectionHandler.class.getResource("/myself/projects/mygallery/style.css").toString());

            stage.setScene(scene);

            ViewWindowController viewWindowController = loader.getController();
            viewWindowController.init(viewItem, stage);

            stage.show();
        }
        catch(IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void close()
    {
        Main.close();
    }
}