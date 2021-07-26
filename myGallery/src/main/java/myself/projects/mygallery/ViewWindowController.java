package myself.projects.mygallery;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class ViewWindowController
{
    @FXML
    ImageView imageView;

    public void init(ViewItem viewItem)
    {
        imageView.setImage(new Image("file:" + viewItem.getPath()));
    }
}