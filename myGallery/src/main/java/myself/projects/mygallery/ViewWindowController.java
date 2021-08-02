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
import javafx.scene.media.MediaPlayer.Status;
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

    double maxHeight = Main.screenHeight * 0.75, maxWidth = Main.screenWidth * 0.75;
    Stage stage;

    int btnSize = 40;
    double graphicRatio = btnSize * 0.6;
    ImageView pauseImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/pause.png").toString()),
              playImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/play.png").toString());

    public void init(ViewItem viewItem, Stage stage)
    {
        this.stage = stage;
        stage.setOnCloseRequest(e -> close());

        pauseImg.setFitHeight(graphicRatio);
        pauseImg.setFitWidth(graphicRatio);
        playImg.setFitHeight(graphicRatio);
        playImg.setFitWidth(graphicRatio);

        btnPlay.setGraphic(pauseImg);
        btnPlay.setPrefHeight(btnSize);
        btnPlay.setPrefWidth(btnSize);

        Node node;

        if(MediaUtils.isImage(viewItem.getType()) || viewItem.getType().equals("gif"))
        {
            Image image = new Image("file:" + viewItem.getPath());
            imageView.setImage(image);

            //constrains window size
            double height = image.getHeight(), width = image.getWidth();

            if(height > maxHeight)
            {
                width *= maxHeight / height;
                height = maxHeight;
            }
            if(width > maxWidth)
            {
                height *= maxWidth / width;
                width = maxWidth;
            }

            imageView.setFitHeight(height);
            imageView.setFitWidth(width);

            stage.setOnShown(e ->
            {
                imageView.fitHeightProperty().bind(stage.getScene().heightProperty());
                imageView.fitWidthProperty().bind(stage.getScene().widthProperty());
            });

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
                double yComp = stage.getHeight() - stage.getScene().getHeight(),
                       xComp = stage.getWidth() - stage.getScene().getWidth();

                double height = mediaPlayer.getMedia().getHeight(), width = mediaPlayer.getMedia().getWidth();

                if(height > maxHeight)
                {
                    width *= maxHeight / height;
                    height = maxHeight;
                }
                if(width > maxWidth)
                {
                    height *= maxWidth / width;
                    width = maxWidth;
                }

                mediaView.setFitHeight(height);
                mediaView.setFitWidth(width);
                mediaView.fitHeightProperty().bind(stage.getScene().heightProperty());
                mediaView.fitWidthProperty().bind(stage.getScene().widthProperty());

                stage.setHeight(height + yComp);
                stage.setWidth(width + xComp);
                stage.centerOnScreen();
            });

            controls.setDisable(false);
            controls.setVisible(true);
            node = mediaView;
        }

        node.setDisable(false);
        node.setVisible(true);
    }

    @FXML
    private void btnPlay()
    {
        boolean playing = mediaView.getMediaPlayer().getStatus().equals(Status.PLAYING);

        if(playing)
        {
            mediaView.getMediaPlayer().pause();
            btnPlay.setGraphic(playImg);
        }
        else
        {
            mediaView.getMediaPlayer().play();
            btnPlay.setGraphic(pauseImg);
        }
    }

    private void close()
    {
        if(!mediaView.isDisable())
            mediaView.getMediaPlayer().dispose();
    }
}