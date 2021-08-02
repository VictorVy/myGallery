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
    private final Label lblEmpty = new Label("Drag files or press Add");

    @FXML
    private TableView<ViewItem> detailsView;
    @FXML
    private TableColumn<ViewItem, String> nameColumn, typeColumn, pathColumn, cDateColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        detailsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        detailsView.setPlaceholder(new Label("Drag files or press Add"));
        detailsView.setOnMouseClicked(e -> SelectionHandler.detailsClicked(detailsView.getSelectionModel().getSelectedItem(), e.getClickCount()));

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        cDateColumn.setCellValueFactory(new PropertyValueFactory<>("cDate"));

        SQLConnector.initialize();
        MediaUtils.initialize();
        updateView();
    }

    //handling buttons
    @FXML
    private void btnAdd()
    {
        //choosing files
        FileChooser fc = new FileChooser();
        fc.setTitle("Add Files");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Files", "*.png", "*.jpg", "*.bmp", "*.gif", "*.mp4", "*.m4v", "*.aif", "*.aiff"));
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
    private void btnRemove()
    {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        ObservableList<ViewItem> selectedItems = FXCollections.observableArrayList();

        if(tab.equals(galleryTab))
            selectedItems = SelectionHandler.getSelected();
        else if(tab.equals(detailsTab))
            selectedItems = detailsView.getSelectionModel().getSelectedItems();

        if(selectedItems.size() > 0)
        {
            Alert alert = Alerts.createRemovalAlert(selectedItems);

            //removing selected items after alerting users
            if(alert.showAndWait().orElse(null) == ButtonType.OK)
            {
                SQLConnector.remove(selectedItems);
                MediaUtils.removeThumbs(selectedItems);
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
    public void btnClear()
    {
        SelectionHandler.deselectAll();
        detailsView.getSelectionModel().clearSelection();
        updateGalleryView();
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
        if(MediaUtils.wrongFiles(event.getDragboard().getFiles()).size() == 0)
        {
            SQLConnector.insert(MediaUtils.filesToViewItems(event.getDragboard().getFiles()));
            MediaUtils.createThumbs(MediaUtils.filesToViewItems(event.getDragboard().getFiles()), 150);

            updateView();
        }
        else
            Alerts.createDragAlert(MediaUtils.wrongFiles(event.getDragboard().getFiles())).showAndWait();
    }

    //updates the view
    public void updateView()
    {
        if(galleryTab.isSelected())
        {
            //sync selection
            try { SelectionHandler.setSelected(detailsView.getSelectionModel().getSelectedItems()); } //hmm... try-catch only necessary on startup...
            catch (Exception e) { System.out.println("null"); }

            updateGalleryView();
        }
        else if(detailsTab.isSelected())
        {
            ObservableList<ViewItem> viewItems = SQLConnector.getDBItems();
            detailsView.setItems(SQLConnector.getDBItems());

            //sync selection
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

                imageViewWrapper.setOnMouseClicked(e -> SelectionHandler.galleryClicked(vi, e.isShiftDown(), e.isControlDown(), e.getButton(), e.getClickCount()));

                if(SelectionHandler.isSelected(vi))
                    imageViewWrapper.getStyleClass().add("image-view-border");

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
        try //dreadful try-catch... necessary?
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
}