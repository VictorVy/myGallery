package myself.projects.mygallery;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewWindowController
{
    ViewItem viewItem;

//    @FXML
    BorderPane borderPane = new BorderPane();
//    @FXML
    ImageView imageView;

    public void show(ViewItem viewItem) throws IOException
    {
        this.viewItem = viewItem;
        imageView = new javafx.scene.image.ImageView("file:" + viewItem.getPath());
        borderPane.centerProperty().setValue(imageView);

//        Parent root = FXMLLoader.load(getClass().getResource("/myself/projects/mygallery/viewWindow.fxml"));

        Stage stage = new Stage();
        stage.setTitle(viewItem.getName() + "." + viewItem.getType());
        stage.getIcons().add(new Image(getClass().getResource("/myself/projects/mygallery/images/gallery.png").toString()));

        Scene scene = new Scene(borderPane);
        scene.getStylesheets().add(getClass().getResource("/myself/projects/mygallery/style.css").toString());

        stage.setScene(scene);
        stage.show();
    }
}