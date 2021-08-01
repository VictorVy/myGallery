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

    int maxHeight = (int)(Main.screenHeight * 0.5), maxWidth = (int)(Main.screenWidth * 0.5);
    Stage stage;

    int btnSize = 40;
    double graphicRatio = btnSize * 0.6;
    ImageView pauseImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/pause.png").toString()),
              playImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/play.png").toString());

    double yComp, xComp;

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
            int height = (int)image.getHeight(), width = (int)image.getWidth();
            System.out.println(height + " by " + width);
            if(height > maxHeight)
            {
                height = maxHeight;
                width *= (double)height / maxHeight;
            }
            if(width > maxWidth)
            {
                width = maxWidth;
                height *= (double)width / maxWidth;
            }
            System.out.println("to: " + height + " by " + width);
            imageView.setFitHeight(height);
            imageView.setFitWidth(width);

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
                yComp = stage.getHeight() - stage.getScene().getHeight();
                xComp = stage.getWidth() - stage.getScene().getWidth();

                int height = mediaPlayer.getMedia().getHeight(), width = mediaPlayer.getMedia().getWidth();
                System.out.println(height + " by " + width);
                if(height > maxHeight)
                {
                    height = maxHeight;
                    width *= (double)height / maxHeight;
                }
                if(width > maxWidth)
                {
                    width = maxWidth;
                    height *= (double)width / maxWidth;
                }
                System.out.println("to: " + height + " by " + width);
                mediaView.setFitHeight(height);
                mediaView.setFitWidth(width);

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