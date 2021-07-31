package myself.projects.mygallery;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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
    @FXML
    private VBox controls;

    @FXML
    private Button btnPlay;

    int maxHeight = 680, maxWidth = 1520;
    Stage stage;

    int btnSize = 40;
    ImageView pauseImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/pause.png").toString()),
              playImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/play.png").toString());

    public void init(ViewItem viewItem, Stage stage)
    {
        this.stage = stage;
        stage.setOnCloseRequest(e -> close());

        btnPlay.setGraphic(pauseImg);

        Node node;

        if(MediaUtils.isImage(viewItem.getType()) || viewItem.getType().equals("gif"))
        {
            Image image = new Image("file:" + viewItem.getPath());
            imageView.setImage(image);
            //constrains window size
            imageView.setFitHeight(Math.min(image.getHeight(), maxHeight));
            imageView.setFitWidth(Math.min(image.getWidth(), maxWidth));

            node = imageView;
        }
        else
        {
            MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File(viewItem.getPath()).toURI().toString()));
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setAutoPlay(true);
            //constrains window size
            mediaPlayer.setOnReady(() ->
            {
                int height = Math.min(mediaPlayer.getMedia().getHeight(), maxHeight),
                    width = Math.min(mediaPlayer.getMedia().getWidth(), maxWidth);

                stage.setHeight(height);
                stage.setWidth(width);
                stage.centerOnScreen();
            });

            controls.setDisable(false);
            controls.setVisible(true);
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