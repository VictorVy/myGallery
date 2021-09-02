package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MainController
{
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab galleryTab, detailsTab;

    @FXML
    private ScrollPane galleryScroll;
    @FXML
    private FlowPane galleryView;
    private final Label lblEmpty = new Label("Drag and drop or press \"+\"");
    public boolean galleryHover;

    @FXML
    private TableView<ViewItem> detailsView;
    @FXML
    private TableColumn<ViewItem, String> nameColumn, typeColumn, pathColumn, cDateColumn, aDateColumn;
    private TableRow<ViewItem> clickedDetailsRow;

    private MenuItem openMenuItem, viewInfoMenuItem, removeMenuItem;
    private Menu selectionMenu;

    @FXML
    private ToggleGroup sortToggleGroup;
    public String sortBy = "aDate";
    @FXML
    private MenuButton sortDirBtn;
    @FXML
    private RadioMenuItem ascSortDir;
    public boolean ascending = true;

    @FXML
    private MenuItem manageTagsMenuItem;

    private final ImageView sortDirImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/sortDir.png").toString());

    public void init()
    {
        detailsViewInit();
        viewContextMenuInit();

        //updates/synchronizes selections between views
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> //better than onSelectionChanged
        {
            if(newValue.equals(galleryTab) && !detailsView.getSelectionModel().getSelectedItems().isEmpty())
                SelectionHandler.findAndSelect(detailsView.getSelectionModel().getSelectedItems());
            else if(newValue.equals(detailsTab) && SelectionHandler.getSelected().size() > 0)
            {
                detailsView.getSelectionModel().clearSelection();

                for(ViewItem vi : SelectionHandler.getSelected())
                    detailsView.getSelectionModel().select(ViewItem.indexOf(getViewItems(), vi));
            }
        });

        //controls
        sortDirImg.setPreserveRatio(true);
        sortDirImg.setFitHeight(16); //hmm...
        sortDirBtn.setGraphic(sortDirImg);

        SQLConnector.initialize();
        MediaUtils.initialize();
        SelectionHandler.initialize();
        tagManagerInit();
        updateViews();
    }
    private void detailsViewInit()
    {
        detailsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        detailsView.setPlaceholder(new Label("Drag files or press Add"));
        //setting cell values
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        cDateColumn.setCellValueFactory(new PropertyValueFactory<>("cDate"));
        aDateColumn.setCellValueFactory(new PropertyValueFactory<>("aDate"));

        //rowFactory for better input detection
        detailsView.setRowFactory(tv ->
        {
            TableRow<ViewItem> row = new TableRow<>();

            //prepares context menus
            ContextMenu contextMenu = new ContextMenu();
            MenuItem removeMenuItem = new MenuItem("Remove");
            removeMenuItem.setOnAction(e -> Main.mainController.removeFiles());
            contextMenu.getItems().add(removeMenuItem);
            //handles clicks
            row.setOnMouseClicked(e -> clickedDetailsRow = row);

            return row;
        });
    }
    private void viewContextMenuInit()
    {
        openMenuItem = new MenuItem("Open");
        openMenuItem.setOnAction(e -> showItems(galleryTab.isSelected() ? SelectionHandler.getSelected() : detailsView.getSelectionModel().getSelectedItems()));

        MenuItem addMenuItem = new MenuItem("Add...");
        addMenuItem.setOnAction(e -> addFiles());

        selectionMenu = new Menu("Selection");
        MenuItem selectAll = new MenuItem("Select all");
        selectAll.setOnAction(e -> selectAll());
        MenuItem clearSelection = new MenuItem("Clear");
        clearSelection.setOnAction(e -> clearSelection());
        selectionMenu.getItems().addAll(selectAll, clearSelection);

        viewInfoMenuItem = new MenuItem("View info");
        viewInfoMenuItem.setOnAction(e -> showInfos(galleryTab.isSelected() ? SelectionHandler.getSelected() : detailsView.getSelectionModel().getSelectedItems()));

        removeMenuItem = new MenuItem("Remove");
        removeMenuItem.setOnAction(e -> removeFiles());

        ContextMenu viewContextMenu = new ContextMenu(openMenuItem, addMenuItem, selectionMenu, viewInfoMenuItem, removeMenuItem);
        galleryScroll.setContextMenu(viewContextMenu);
        detailsView.setContextMenu(viewContextMenu);
    }
    private void tagManagerInit()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/tag-manager.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Tag Manager");
            stage.getIcons().add(new Image(getClass().getResource("/myself/projects/mygallery/images/gallery.png").toString()));

            Scene scene = new Scene(loader.load(), 275, 400);
            scene.getStylesheets().add(getClass().getResource("/myself/projects/mygallery/style.css").toString());

            stage.initModality(Modality.APPLICATION_MODAL); //secret sauce
            stage.setScene(scene);

            TagManagerController tagManagerController = loader.getController();
            tagManagerController.init();

            manageTagsMenuItem.setOnAction(e -> stage.show() /*yet to figure out why showAndWait doesn't work*/);
        }
        catch(IOException e) { e.printStackTrace(); }
    }

    //table column context menus
    @FXML private void nameColHide() { nameColumn.setVisible(false); }
    @FXML private void typeColHide() { typeColumn.setVisible(false); }
    @FXML private void pathColHide() { pathColumn.setVisible(false); }
    @FXML private void cDateColHide() { cDateColumn.setVisible(false); }
    @FXML private void aDateColHide() { aDateColumn.setVisible(false); }

    @FXML
    private void addFiles()
    {
        //choosing files
        FileChooser fc = new FileChooser();
        fc.setTitle("Add Files");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Files", "*.png", "*.jpg", "*.bmp", "*.gif", "*.mp4", "*.m4v", "*.mp3", "*.wav", "*.aif", "*.aiff"));
        List<File> files = fc.showOpenMultipleDialog(Main.stage);

        if(files != null)
        {
            //inserts items into the db
            SQLConnector.insertFiles(MediaUtils.filesToViewItems(files));
            //generating thumbnails
            MediaUtils.createThumbs(MediaUtils.filesToViewItems(files), 150);

            updateViews();
        }
    }

    @FXML
    public void removeFiles()
    {
        ObservableList<ViewItem> selectedItems = FXCollections.observableArrayList();

        if(galleryTab.isSelected())
            selectedItems = SelectionHandler.getSelected();
        else if(detailsTab.isSelected())
            selectedItems = detailsView.getSelectionModel().getSelectedItems();

        remove(selectedItems);
    }
    @FXML
    private void removeAll() { remove(SQLConnector.getFiles()); }
    private void remove(ObservableList<ViewItem> items)
    {
        if(!items.isEmpty())
        {
            ObservableList<String> names = FXCollections.observableArrayList();

            for(ViewItem vi : items)
                names.add(vi.getName() + "." + vi.getType());

            Alert alert = Dialogs.createRemovalAlert(names);

            //removing selected items after alerting users
            if(alert.showAndWait().orElse(null) == ButtonType.OK)
            {
                SQLConnector.removeFiles(items);
                MediaUtils.removeThumbs(items);
                updateViews();
            }
        }
    }

    @FXML
    private void syncFiles()
    {
        if(Objects.requireNonNull(SQLConnector.getFiles()).size() > 0)
        {
            //checking if items in the db exist; if not, remove them
            ObservableList<ViewItem> viewItems = SQLConnector.getFiles();
            ObservableList<ViewItem> toRemove = FXCollections.observableArrayList();

            for(ViewItem vi : viewItems)
                if(!(new File(vi.getPath()).exists())) toRemove.add(vi);

            if(toRemove.size() > 0)
            {
                SQLConnector.removeFiles(toRemove);
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
    @FXML
    private void detailsClicked(MouseEvent e)
    {
        //messy if statements... clean up later?

        if(!detailsView.getItems().isEmpty() && clickedDetailsRow.isEmpty())
            clearSelection();

        if(!clickedDetailsRow.isEmpty() && e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2)
            showItem(clickedDetailsRow.getItem());
    }
    @FXML
    private void viewContextMenuRequested()
    {
        if(galleryView.getChildren().isEmpty() || detailsView.getItems().isEmpty())
        {
            openMenuItem.setDisable(true);
            selectionMenu.setDisable(true);
            viewInfoMenuItem.setDisable(true);
            removeMenuItem.setDisable(true);
        }
        else
        {
            boolean isNotLegit = (galleryTab.isSelected() && !galleryHover) || (detailsTab.isSelected() && clickedDetailsRow.isEmpty());

            openMenuItem.setDisable(isNotLegit);
            selectionMenu.setDisable(false);
            viewInfoMenuItem.setDisable(isNotLegit);
            removeMenuItem.setDisable(isNotLegit);
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
        List<File> dragFiles = event.getDragboard().getFiles();

        if(MediaUtils.wrongFiles(dragFiles).size() == 0)
        {
            SQLConnector.insertFiles(MediaUtils.filesToViewItems(dragFiles));
            MediaUtils.createThumbs(MediaUtils.filesToViewItems(dragFiles), 150);

            updateViews();
        }
        else
            Dialogs.createDragAlert(MediaUtils.wrongFiles(dragFiles)).showAndWait();
    }

    //updates the view

    public void updateViews()
    {
        updateGalleryView();
        updateDetailsView();
    }

    public void updateGalleryView()
    {
        ObservableList<ViewItem> viewItems = SQLConnector.getFiles();
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
    private void updateDetailsView() { detailsView.setItems(SQLConnector.getFiles()); }

    public ObservableList<ViewItem> getViewItems()
    {
        ObservableList<ViewItem> items = FXCollections.observableArrayList();

        for(int i = 0; i < galleryView.getChildren().size(); i++)
            items.add(((ViewItemWrapper) galleryView.getChildren().get(i)).getViewItem());

        return items;
    }

    public void showItem(ViewItem viewItem)
    {
        try //necessary?
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/view-window.fxml"));

            Stage stage = new Stage();
            stage.setTitle(viewItem.getName() + "." + viewItem.getType());
            stage.getIcons().add(new Image(getClass().getResource("/myself/projects/mygallery/images/gallery.png").toString()));

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/myself/projects/mygallery/style.css").toString());

            stage.setScene(scene);

            ViewWindowController viewWindowController = loader.getController();
            viewWindowController.init(viewItem, stage);

            stage.show();
        }
        catch(IOException e) { e.printStackTrace(); }
    }
    private void showItems(ObservableList<ViewItem> items) { for(ViewItem vi : items) showItem(vi); }

    private void showInfo(ViewItem viewItem) //TODO: reduce redundancy with showItem
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/item-info.fxml"));

            Stage stage = new Stage();
            stage.setTitle(viewItem.getName() + "." + viewItem.getType());
            stage.getIcons().add(new Image(getClass().getResource("/myself/projects/mygallery/images/gallery.png").toString()));

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/myself/projects/mygallery/style.css").toString());

            stage.setScene(scene);

            ItemInfoController itemInfoController = loader.getController();
            itemInfoController.init();

            stage.show();
        }
        catch(IOException e) { e.printStackTrace(); }
    }
    private void showInfos(ObservableList<ViewItem> items) { for(ViewItem vi : items) showInfo(vi); }

    @FXML
    private void close() { Main.close(); }

//    @FXML
//    private void test2()
//    {
//        String[] itemNames = new String[3];
//        String[] tagNames = new String[3];
//
//        itemNames[0] = "D:\\Pictures\\Saved Pictures\\GitHub.png";
//        itemNames[1] = "D:\\Pictures\\Saved Pictures\\gOn.PNG";
//        itemNames[2] = "D:\\Pictures\\Saved Pictures\\kirito.PNG";
//
//        tagNames[0] = "test1";
//        tagNames[1] = "test2";
//        tagNames[2] = "test3";
//
//        SQLConnector.insertXRef(itemNames, tagNames);
//    }
}