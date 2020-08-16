package projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    //injecting controls
    @FXML
    private TableView<ViewItem> detailView;
    @FXML
    private TableColumn<ViewItem, String> nameColumn;
    @FXML
    private TableColumn<ViewItem, String> pathColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        detailView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        detailView.setPlaceholder(new Label("Drag files or press Add"));

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));

        SQLConnector.initialize();
        updateView();
    }

    //handling buttons
    @FXML
    private void btnAdd() {
        //choosing files
        FileChooser fc = new FileChooser();
        fc.setTitle("Add Files");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Files", "*.png", "*.jpg", "*.gif", "*.mp4", "*.mov", "*.mkv", "*.webm", "*.avi"));
        List<File> files = fc.showOpenMultipleDialog(Main.mainScene.getWindow());

        if (files != null) {
            //inserts items into the db
            SQLConnector.insert(filesToViewItems(files));
            updateView();
        }
    }

    @FXML
    private void btnRemove() {
        ObservableList<ViewItem> selectedItems = detailView.getSelectionModel().getSelectedItems();

        if (selectedItems.size() > 0) {
            Alert alert = createRemovalAlert(selectedItems);

            //removing selected items after alerting users
            if (alert.showAndWait().orElse(null) == ButtonType.OK) {
                SQLConnector.remove(selectedItems);
                updateView();
            }
        }
    }

    @FXML
    private void btnRefresh() {
        if (detailView.getItems().size() > 0) {
            //checking if items in the db exists; if not, remove them
            ObservableList<ViewItem> viewItems = SQLConnector.getDBItems();
            ObservableList<ViewItem> toRemove = FXCollections.observableArrayList();

            for (ViewItem vi : viewItems)
                if (!(new File(vi.getPath()).exists())) toRemove.add(vi);

            SQLConnector.remove(toRemove);
            updateView();
        }
    }

    //handling dragging files into table
    @FXML
    private void dragOverTable(DragEvent event) {
        if (event.getDragboard().hasFiles())
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
    }
    @FXML
    private void dragDropTable(DragEvent event) {
        if (areMediaFiles(event.getDragboard().getFiles()).size() == 0) {
            SQLConnector.insert(filesToViewItems(event.getDragboard().getFiles()));
            updateView();
        } else {
            Alert alert = createDragAlert(areMediaFiles(event.getDragboard().getFiles()));
            alert.show();
        }
    }

    //updates the gui
    private void updateView() {
        detailView.setItems(SQLConnector.getDBItems());
    }

    //returns a list of ViewItems from a list of Files
    private ObservableList<ViewItem> filesToViewItems(List<File> files) {
        ObservableList<ViewItem> viewItems = FXCollections.observableArrayList();

        for (File f : files)
            viewItems.add(new ViewItem(f.getName(), f.getAbsolutePath()));

        return viewItems;
    }

    //checks if all the files in a list are the correct type
    private ObservableList<ViewItem> areMediaFiles(List<File> files) {
        ObservableList<ViewItem> items = filesToViewItems(files);
        ObservableList<ViewItem> notMedia = FXCollections.observableArrayList();

        for (ViewItem vi : items) {
            String ext = vi.getName().substring(vi.getName().indexOf('.'));

            if (!ext.equals(".png") && !ext.equals(".jpg") && !ext.equals(".gif") && !ext.equals(".mp4") && !ext.equals(".mov") && !ext.equals(".mkv") && !ext.equals(".webm") && !ext.equals(".avi"))
                notMedia.add(vi);
        }

        return notMedia;
    }

    //returns an alert to confirm removal with user
    private Alert createRemovalAlert(ObservableList<ViewItem> selectedItems) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Confirm Removal");
        alert.setHeaderText(null);
        alert.setContentText("Remove " + selectedItems.size() + " item(s)?");
        alert.setResizable(false);
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("/projects/mygallery/images/bin.png"));

        //creating and setting expandable content
        StringBuilder names = new StringBuilder("Items: \n");

        String prefix = "";
        for (ViewItem vi : selectedItems) {
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
    private Alert createDragAlert(ObservableList<ViewItem> notMedia) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Incorrect Filetype");
        alert.setHeaderText(null);
        alert.setContentText("You can only add image or video files!");
        alert.setResizable(false);
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("/projects/mygallery/images/bin.png"));

        //creating and setting expandable content
        StringBuilder names = new StringBuilder("Items: \n");

        String prefix = "";
        for (ViewItem vi : notMedia) {
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