package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainController implements Initializable
{
    //injecting controls
    @FXML private TabPane tabPane;
    @FXML private Tab galleryTab, detailsTab;

    @FXML private FlowPane galleryView;
    private final Label lblEmpty = new Label("Drag files or press Add");

    @FXML private TableView<ViewItem> detailsView;
    @FXML private TableColumn<ViewItem, String> nameColumn, pathColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        detailsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        detailsView.setPlaceholder(new Label("Drag files or press Add"));

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));

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
        ObservableList<ViewItem> selectedItems = detailsView.getSelectionModel().getSelectedItems();

        if(selectedItems.size() > 0)
        {
            Alert alert = createRemovalAlert(selectedItems);

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
            //checking if items in the db exists; if not, remove them
            ObservableList<ViewItem> viewItems = SQLConnector.getDBItems();
            ObservableList<ViewItem> toRemove = FXCollections.observableArrayList();

            for(ViewItem vi : viewItems)
                if(!(new File(vi.getPath()).exists())) toRemove.add(vi);

            if(toRemove.size() > 0) { SQLConnector.remove(toRemove); }

            updateView();
        }
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
            createDragAlert(MediaUtils.wrongFiles(event.getDragboard().getFiles())).showAndWait();
    }

    //updates the view
    public void updateView()
    {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();

        if(tab.equals(galleryTab))
        {
            ObservableList<ViewItem> viewItems = SQLConnector.getDBItems();
            galleryView.getChildren().clear();
            galleryView.getChildren().add(lblEmpty);

            if(viewItems != null && !viewItems.isEmpty())
            {
                galleryView.getChildren().clear();

                for(ViewItem vi : viewItems)
                    galleryView.getChildren().add(new ImageView("file:" + vi.getThumb()));
            }
        }
        else if(tab.equals(detailsTab))
            detailsView.setItems(SQLConnector.getDBItems());
    }

    //returns an alert to confirm removal with user
    private Alert createRemovalAlert(ObservableList<ViewItem> selectedItems)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Confirm Removal");
        alert.setHeaderText(null);
        alert.setContentText("Remove " + selectedItems.size() + " item(s)?");
        alert.setResizable(false);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(getClass().getResource("/myself/projects/mygallery/images/bin.png").toString()));

        //creating and setting expandable content
        StringBuilder names = new StringBuilder("Items: \n");

        String prefix = "";
        for(ViewItem vi : selectedItems)
        {
            names.append(prefix);
            prefix = ", ";
            names.append(vi.getName());
        }

        Label itemNames = new Label(names.toString() + '.');
        itemNames.setWrapText(true);

        alert.getDialogPane().setExpandableContent(itemNames);
        alert.getDialogPane().setPrefWidth(0);

        return alert;
    }

    //returns an error alert when user drags non-media file
    private Alert createDragAlert(ObservableList<ViewItem> notMedia)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Incorrect Filetype");
        alert.setHeaderText(null);
        alert.setContentText("You can only add image or video files!");
        alert.setResizable(false);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(getClass().getResource("/myself/projects/mygallery/images/bin.png").toString()));

        //creating and setting expandable content
        StringBuilder names = new StringBuilder("Items: \n");

        String prefix = "";
        for(ViewItem vi : notMedia)
        {
            names.append(prefix);
            prefix = ", ";
            names.append(vi.getName());
        }

        Label itemNames = new Label(names.toString() + '.');
        itemNames.setWrapText(true);

        alert.getDialogPane().setExpandableContent(itemNames);
        alert.getDialogPane().setPrefWidth(0);

        return alert;
    }
}