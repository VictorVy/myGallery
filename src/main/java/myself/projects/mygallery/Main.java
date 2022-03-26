package myself.projects.mygallery;

import com.sun.javafx.css.Rule;
import com.sun.javafx.css.StyleManager;
import com.sun.javafx.css.parser.CSSParser;
import javafx.application.Application;
import com.sun.javafx.css.Stylesheet;
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

    public static String stylesheet = String.valueOf(Main.class.getResource("/myself/projects/mygallery/style.css"));

    public static void main(String[] args) { launch(args); }

    public void start(Stage stage) throws Exception
    {
        screenWidth = Screen.getPrimary().getBounds().getWidth();
        screenHeight = Screen.getPrimary().getBounds().getHeight();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/main.fxml"));
        Parent root = loader.load();
        mainController = loader.getController();
        mainController.init(); //regular initialize doesn't work...

        Scene scene = new Scene(root, screenWidth * 0.75, screenHeight * 0.75);
        scene.getStylesheets().add(stylesheet);
        allScenes.add(scene.getRoot());

//        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
//        StyleManager.getInstance().addUserAgentStylesheet(stylesheet);

//        CSSParser parser = new CSSParser();
//        Stylesheet ss = parser.parse(getClass().getResource("/myself/projects/mygallery/style.css"));
//        Rule rootRule = ss.getRules().get(0);
//        rootRule.getDeclarations().stream().filter(d -> d.getProperty().equals("-fx-font-size")).map(d -> 10);
//        ss.getRules().remove(0);
//        rootRule.getDeclarations().get(0);

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
    }

    public static void close()
    {
        SQLConnector.close();
        stage.close();
    }
}