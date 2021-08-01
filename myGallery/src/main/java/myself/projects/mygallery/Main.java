package myself.projects.mygallery;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application
{
    public static double screenWidth, screenHeight;

    public static Scene mainScene;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage mainStage) throws Exception
    {
        screenWidth = Screen.getPrimary().getBounds().getWidth();
        screenHeight = Screen.getPrimary().getBounds().getHeight();

        Parent root = FXMLLoader.load(getClass().getResource("/myself/projects/mygallery/main.fxml"));

        mainScene = new Scene(root, 1280, 720);
        mainScene.getStylesheets().add(getClass().getResource("/myself/projects/mygallery/style.css").toString());

        mainStage.setTitle("myGallery");
        mainStage.getIcons().add(new Image(getClass().getResource("/myself/projects/mygallery/images/gallery.png").toString()));
        mainStage.setOnCloseRequest(e -> SQLConnector.close());
        mainStage.setScene(mainScene);
        mainStage.show();
    }
}