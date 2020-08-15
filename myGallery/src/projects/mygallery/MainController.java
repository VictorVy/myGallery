package projects.mygallery;

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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable
{
    //injecting controls
    @FXML
    private TableView<ViewItem> detailView;
    @FXML
    private TableColumn<ViewItem, String> nameColumn;
    @FXML
    private TableColumn<ViewItem, String> pathColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        detailView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        detailView.setPlaceholder(new Label("Drag files or press Add"));

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));

        SQLConnector.initialize();
        updateView();
    }

    //handling buttons
    @FXML
    private void btnAdd()
    {
        //choosing files
        FileChooser fc = new FileChooser();
        fc.setTitle("Add Files");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Files", "*.png", "*.jpg", "*.gif", "*.mp4", "*.mov", "*.mkv", "*.webm", "*.avi"));
        List<File> files = fc.showOpenMultipleDialog(Main.mainScene.getWindow());

        if(files != null) //inserts items into the db
        {
            SQLConnector.insert(filesToViewItems(files));
            updateView();
        }
    }
    @FXML
    private void btnRemove()
    {
        ObservableList<ViewItem> selectedItems = detailView.getSelectionModel().getSelectedItems();

        if(selectedItems.size() > 0)
        {
            //creating alert
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

            if (alert.showAndWait().orElse(null) == ButtonType.OK) //removing selected items after alerting users
            {
                SQLConnector.remove(selectedItems);
                updateView();
            }
        }
    }

    //handling dragging files into table
    @FXML
    private void dragOverTable(DragEvent event)
    {
        if(event.getDragboard().hasFiles())
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
    }
    @FXML
    private void dragDropTable(DragEvent event)
    {
        SQLConnector.insert(filesToViewItems(event.getDragboard().getFiles()));
        updateView();
    }

    private void updateView() { detailView.setItems(SQLConnector.getDBItems()); } //updates the gui

    private ObservableList<ViewItem> filesToViewItems(List<File> files) //returns a list of ViewItems from a list of Files
    {
        ObservableList<ViewItem> viewItems = FXCollections.observableArrayList();

        for(File f : files)
            viewItems.add(new ViewItem(f.getName(), f.getAbsolutePath()));

        return viewItems;
    }
}