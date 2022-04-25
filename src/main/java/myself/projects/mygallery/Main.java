package myself.projects.mygallery;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application
{
    static MainController mainController;

    public static double screenWidth, screenHeight;

    public static Stage stage;

    public static ObservableList<Parent> allScenes = FXCollections.observableArrayList();

    public static String stylesheet_light = String.valueOf(Main.class.getResource("/myself/projects/mygallery/style-light.css")),
                         stylesheet_dark = String.valueOf(Main.class.getResource("/myself/projects/mygallery/style-dark.css"));

    public static void main(String[] args) { launch(args); }

    public void start(Stage stage) throws Exception
    {
        screenWidth = Screen.getPrimary().getBounds().getWidth();
        screenHeight = Screen.getPrimary().getBounds().getHeight();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/main.fxml"));
        Parent root = loader.load();
        mainController = loader.getController();
        mainController.init(); //regular initialize doesn't work...

//        Application.setUserAgentStylesheet(); //overrides modena
//        StyleManager.getInstance().addUserAgentStylesheet(stylesheetTest); //would use but for stubborn galleryView background colour

        Scene scene = new Scene(root, screenWidth * 0.75, screenHeight * 0.75);
        scene.getRoot().getStylesheets().add(stylesheet_light);
        allScenes.add(scene.getRoot());

        Main.stage = stage;
        stage.setTitle("myGallery");
        stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/gallery.png"))));
        stage.setOnCloseRequest(e ->
        {
            e.consume();
            close();
        });
        stage.setScene(scene);
        stage.show();

        //update style according to prefs (can't be in MainController)
        MiscUtils.updateStyles();
    }

    public static void close()
    {
        SQLConnector.close();
        stage.close();
    }
}