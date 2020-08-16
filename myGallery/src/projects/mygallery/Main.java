package projects.mygallery;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    public static Scene mainScene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage mainStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));

        mainScene = new Scene(root);
        mainScene.getStylesheets().add("/projects/mygallery/style.css");

        mainStage.setTitle("myGallery");
        mainStage.getIcons().add(new Image("/projects/mygallery/images/gallery.png"));
        mainStage.setScene(mainScene);
        mainStage.show();
    }
}
