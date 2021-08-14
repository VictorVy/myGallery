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

    private static Stage stage;
    public static Scene mainScene;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception
    {
        screenWidth = Screen.getPrimary().getBounds().getWidth();
        screenHeight = Screen.getPrimary().getBounds().getHeight();

        Parent root = FXMLLoader.load(getClass().getResource("/myself/projects/mygallery/main.fxml"));

        mainScene = new Scene(root, screenWidth * 0.75, screenHeight * 0.75);
        mainScene.getStylesheets().add(getClass().getResource("/myself/projects/mygallery/style.css").toString());

        this.stage = stage;
        stage.setTitle("myGallery");
        stage.getIcons().add(new Image(getClass().getResource("/myself/projects/mygallery/images/gallery.png").toString()));
        stage.setOnCloseRequest(e ->
        {
            e.consume();
            close();
        });
        stage.setScene(mainScene);
        stage.show();
    }

    public static void close()
    {
        SQLConnector.close();
        stage.close();
    }
}