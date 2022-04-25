package myself.projects.mygallery;

import com.sun.javafx.css.StyleManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainController
{
    @FXML
    private TabPane viewTabPane;
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

    private MenuItem openMenuItem, viewInfoMenuItem, editTagsMenuItem, removeMenuItem;
    private Menu selectionMenu;

    @FXML
    private TextField searchBar;
    @FXML
    private SplitMenuButton searchBtn;
    @FXML
    private CheckBox searchAll;
    @FXML
    public CheckBox searchName, searchType, searchTags;

    @FXML
    private ToggleGroup sortToggleGroup;
    public String sortBy = "aDate";
    @FXML
    private MenuButton sortDirBtn;
    @FXML
    private RadioMenuItem ascSortDir;
    boolean ascending = true;

    @FXML
    private Button showPrefsBtn;

    public TagManagerController tagManagerController;
    public PreferencesController preferencesController;

//    AddFilesThread addFilesThread = new AddFilesThread();

    private final ImageView sortDirImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/sortDir.png"))),
                            searchImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/search.png"))),
                            settingsImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/settings.png")));

    public int thumbSizeLimit = 150;

    public void init()
    {
        //updates/synchronizes selections between views
        viewTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> //better than onSelectionChanged
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
        searchImg.setPreserveRatio(true);
        searchImg.setFitHeight(16); //hmm...
        searchBtn.setGraphic(searchImg);

        sortDirImg.setPreserveRatio(true);
        sortDirImg.setFitHeight(16); //hmm...
        sortDirBtn.setGraphic(sortDirImg);

        settingsImg.setPreserveRatio(true);
        settingsImg.setFitHeight(16);
        showPrefsBtn.setGraphic(settingsImg);

        //initializers
        detailsViewInit();
        viewContextMenuInit();
        SQLConnector.initialize();
        MiscUtils.initialize();
        SelectionHandler.initialize();
        tagManagerInit();
        preferencesInit();
        updateViews();
    }

    private void detailsViewInit()
    {
        detailsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
            MenuItem removeMenuItem = new MenuItem("Remove Items");
            removeMenuItem.setOnAction(e -> removeFiles());
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

        MenuItem addMenuItem = new MenuItem("Add Items...");
        addMenuItem.setOnAction(e -> addFiles());

        selectionMenu = new Menu("Selection");
        MenuItem selectAll = new MenuItem("Select All");
        selectAll.setOnAction(e -> selectAll());
        MenuItem clearSelection = new MenuItem("Deselect");
        clearSelection.setOnAction(e -> clearSelection());
        selectionMenu.getItems().addAll(selectAll, clearSelection);

        viewInfoMenuItem = new MenuItem("View Info");
        viewInfoMenuItem.setOnAction(e -> showInfos(galleryTab.isSelected() ? SelectionHandler.getSelected() : detailsView.getSelectionModel().getSelectedItems()));

        editTagsMenuItem = new MenuItem("Edit Tags");
        editTagsMenuItem.setOnAction(e -> showTagInfos(galleryTab.isSelected() ? SelectionHandler.getSelected() : detailsView.getSelectionModel().getSelectedItems()));

        removeMenuItem = new MenuItem("Remove");
        removeMenuItem.setOnAction(e -> removeFiles());

        ContextMenu viewContextMenu = new ContextMenu(openMenuItem, addMenuItem, selectionMenu, viewInfoMenuItem, editTagsMenuItem, removeMenuItem);
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
            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/gallery.png"))));

            Scene scene = new Scene(loader.load(), 350, 500);
            scene.getRoot().getStylesheets().add(Main.stylesheetTest);

            stage.initModality(Modality.APPLICATION_MODAL); //secret sauce
            stage.setScene(scene);

            tagManagerController = loader.getController();
            tagManagerController.init(stage);
        }
        catch(IOException e) { e.printStackTrace(); }
    }
    @FXML
    private void manageTags() { tagManagerController.show(); }
    private void preferencesInit()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/preferences.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Preferences");
            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/settings.png"))));
            stage.setResizable(false);

            Scene scene = new Scene(loader.load(), 350, 300);
            scene.getRoot().getStylesheets().add(Main.stylesheetTest);

            stage.initModality(Modality.APPLICATION_MODAL); //secret sauce
            stage.setScene(scene);

            preferencesController = loader.getController();
            preferencesController.init(stage);
        }
        catch(IOException e) { e.printStackTrace(); }
    }
    @FXML
    private void showPrefs() { preferencesController.show(); }

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
            ObservableList<ViewItem> addedItems = MiscUtils.filesToViewItems(files);
            addItems(addedItems, thumbSizeLimit);
        }
    }

    private void addItems(ObservableList<ViewItem> items, int tsl) { new addItemsService(items, tsl).start(); }

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
    private void removeAll() { remove(SQLConnector.searchFiles(searchBar.getText())); }
    private void remove(ObservableList<ViewItem> items)
    {
        if(!items.isEmpty())
        {
            ObservableList<String> names = FXCollections.observableArrayList();

            for(ViewItem vi : items)
                names.add(vi.getName() + "." + vi.getType());

            Alert alert = Dialogs.createRemovalAlert(names, "Confirm Removal", "Remove " + names.size() + " item(s)?");

            //removing selected items after alerting users
            if(alert.showAndWait().orElse(null) == ButtonType.OK)
            {
                SQLConnector.removeFiles(items);
                MiscUtils.removeThumbs(items);
                updateViews();
            }
        }
    }

    @FXML
    private void search() { updateViews(); }
    @FXML
    private void searchKeyPressed(KeyEvent e) { if(e.getCode().equals(KeyCode.ENTER)) search(); }
    @FXML
    private void searchByAll()
    {
        boolean all = searchAll.isSelected();
        searchName.setSelected(all);
        searchType.setSelected(all);
        searchTags.setSelected(all);
        search();
    }
    @FXML
    private void searchBy()
    {
        searchAll.setSelected(searchName.isSelected() && searchType.isSelected() && searchTags.isSelected());
        search();
    }

    @FXML
    private void syncFiles()
    {
        if(SQLConnector.getFiles().size() > 0)
            new syncFilesService().start();
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
        if(!detailsView.getItems().isEmpty())
        {
            if(clickedDetailsRow.isEmpty())
                clearSelection();
            else if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2)
                showItem(clickedDetailsRow.getItem());
        }
    }
    @FXML
    private void viewContextMenuRequested()
    {
        if(galleryView.getChildren().contains(lblEmpty) || detailsView.getItems().isEmpty())
        {
            openMenuItem.setDisable(true);
            selectionMenu.setDisable(true);
            viewInfoMenuItem.setDisable(true);
            editTagsMenuItem.setDisable(true);
            removeMenuItem.setDisable(true);
        }
        else
        {
            boolean isNotLegit = (galleryTab.isSelected() && !galleryHover) || (detailsTab.isSelected() && clickedDetailsRow.isEmpty());

            openMenuItem.setDisable(isNotLegit);
            selectionMenu.setDisable(false);
            viewInfoMenuItem.setDisable(isNotLegit);
            editTagsMenuItem.setDisable(isNotLegit);
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

        if(MiscUtils.wrongFiles(dragFiles).size() == 0)
        {
            ObservableList<ViewItem> list = MiscUtils.filesToViewItems(dragFiles);
            addItems(list, thumbSizeLimit);
        }
        else
            Dialogs.createDragAlert(MiscUtils.wrongFiles(dragFiles)).showAndWait();
    }

    //updates the view
    @FXML
    public void updateViews()
    {
        lblEmpty.setText(SQLConnector.getFiles().isEmpty() ? "Drag and drop or press \"+\"" : "No items found");
        detailsView.setPlaceholder(new Label(lblEmpty.getText()));
        updateGalleryView();
        updateDetailsView();
    }

    public void updateGalleryView()
    {
        ObservableList<ViewItem> viewItems = SQLConnector.searchFiles(searchBar.getText());
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
    private void updateDetailsView() { detailsView.setItems(SQLConnector.searchFiles(searchBar.getText())); }

    public ObservableList<ViewItem> getViewItems()
    {
        ObservableList<ViewItem> items = FXCollections.observableArrayList();
        if(galleryView.getChildren().contains(lblEmpty)) return items;

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
            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/gallery.png"))));

            Scene scene = new Scene(loader.load());
            scene.getRoot().getStylesheets().add(Main.stylesheetTest);

            stage.setScene(scene);

            ViewWindowController viewWindowController = loader.getController();
            viewWindowController.init(viewItem, stage);

            stage.show();
        }
        catch(IOException e) { e.printStackTrace(); }
    }
    private void showItems(ObservableList<ViewItem> items) { for(ViewItem vi : items) showItem(vi); }

    private void showInfo(ViewItem viewItem) //TODO: somehow reduce redundancy with showItem
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/item-info.fxml"));

            Stage stage = new Stage();
            stage.setTitle(viewItem.getName() + "." + viewItem.getType());
            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/gallery.png"))));

            Scene scene = new Scene(loader.load(), 300, 450);
            scene.getRoot().getStylesheets().add(Main.stylesheetTest);

            stage.setScene(scene);

            ItemInfoController itemInfoController = loader.getController();
            itemInfoController.init(viewItem);

            stage.show();
        }
        catch(IOException e) { e.printStackTrace(); }
    }
    private void showInfos(ObservableList<ViewItem> items) { for(ViewItem vi : items) showInfo(vi); }

    private void showTagInfo(ViewItem viewItem) //TODO: somehow reduce redundancy with showItem and showInfo
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/item-info.fxml"));

            Stage stage = new Stage();
            stage.setTitle(viewItem.getName() + "." + viewItem.getType());
            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/gallery.png"))));

            Scene scene = new Scene(loader.load(), 300, 450);
            scene.getRoot().getStylesheets().add(Main.stylesheetTest);

            stage.setScene(scene);

            ItemInfoController itemInfoController = loader.getController();
            itemInfoController.init(viewItem);
            itemInfoController.editTags();

            stage.show();
        }
        catch(IOException e) { e.printStackTrace(); }
    }
    private void showTagInfos(ObservableList<ViewItem> items) { for(ViewItem vi : items) showTagInfo(vi); }

    @FXML
    private void viewThumbnails() { MiscUtils.openThumbsDirectory(); }

    @FXML
    private void helpHelp() { MiscUtils.openBrowserTo("https://github.com/VictorVy/myGallery/wiki"); }
    @FXML
    private void helpSrcCode() { MiscUtils.openBrowserTo("https://github.com/VictorVy/myGallery"); }
    @FXML
    private void helpReleases() { MiscUtils.openBrowserTo("https://github.com/VictorVy/myGallery/releases"); }
    @FXML
    private void helpAbout() { MiscUtils.openBrowserTo("https://github.com/VictorVy/myGallery/wiki"); }

    @FXML
    private void close() { Main.close(); }

    private class addItemsService extends Service<Void>
    {
        ObservableList<ViewItem> items;
        int tsl;

        private addItemsService(ObservableList<ViewItem> items, int tsl)
        {
            this.items = items;
            this.tsl = tsl;

            setOnSucceeded(e ->
            {
                updateViews();
                //selects newly added items TODO: create method
                SelectionHandler.findAndSelect(items);
            });
        }

        @Override
        protected Task<Void> createTask()
        {
            return new Task<Void>()
            {
                @Override
                protected Void call()
                {
                    //inserts items into the db
                    SQLConnector.insertFiles(items);
                    //generates thumbnails
                    MiscUtils.createThumbs(items, tsl);

                    return null;
                }
            };
        }
    }

    private class syncFilesService extends Service<Void>
    {
        private syncFilesService() { setOnSucceeded(e -> updateViews()); }

        @Override
        protected Task<Void> createTask()
        {
            return new Task<Void>()
            {
                @Override
                protected Void call()
                {
                    ObservableList<ViewItem> viewItems = SQLConnector.getFiles();
                    ObservableList<ViewItem> toRemove = FXCollections.observableArrayList(), missingThumbs = FXCollections.observableArrayList();

                    //checking if items in the db exist; if not, remove them

                    for(ViewItem vi : viewItems)
                        if(!(new File(vi.getPath()).exists())) toRemove.add(vi);

                    if(toRemove.size() > 0)
                    {
                        SQLConnector.removeFiles(toRemove);
                        MiscUtils.removeThumbs(toRemove);
                    }

                    //checking if any thumbnails are missing; if yes, create them

                    viewItems = SQLConnector.getFiles();

                    for(ViewItem vi : viewItems)
                        if(!(new File(vi.getThumb()).exists())) missingThumbs.add(vi);

                    if(missingThumbs.size() > 0)
                        MiscUtils.createThumbs(missingThumbs, thumbSizeLimit);

                    return null;
                }
            };
        }
    }
}