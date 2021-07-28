package myself.projects.mygallery;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

public class ViewWindowController
{
    @FXML
    private ImageView imageView;
    @FXML
    private MediaView mediaView;

    int maxHeight = 1820, maxWidth = 980;
    Stage stage;

    public void init(ViewItem viewItem, Stage stage)
    {
        this.stage = stage;
        stage.setOnCloseRequest(e -> close());

        Node node;

        if(MediaUtils.isImage(viewItem.getType()) || viewItem.getType().equals("gif"))
        {
            Image image = new Image("file:" + viewItem.getPath());
            imageView.setImage(image);

            imageView.setFitHeight(Math.min(image.getHeight(), maxHeight));
            imageView.setFitWidth(Math.min(image.getWidth(), maxWidth));

            node = imageView;
        }
        else
        {
            MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File(viewItem.getPath()).toURI().toString()));
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setAutoPlay(true);

            int height = Math.min(mediaPlayer.getMedia().getHeight(), maxHeight),
                width = Math.min(mediaPlayer.getMedia().getWidth(), maxWidth);

            stage.setHeight(height);
            stage.setWidth(width);

            mediaPlayer.setOnReady(() ->
            {
                mediaView.setFitHeight(Math.min(height, maxHeight));
                mediaView.setFitWidth(Math.min(width, maxWidth));
            });

            node = mediaView;
        }

        node.setDisable(false);
        node.setVisible(true);
    }

    private void close()
    {
        if(!mediaView.isDisable())
            mediaView.getMediaPlayer().dispose();
    }
}