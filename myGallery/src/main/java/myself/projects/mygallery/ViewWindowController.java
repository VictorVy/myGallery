package myself.projects.mygallery;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ViewWindowController
{
    @FXML
    private ImageView imageView;
    @FXML
    private MediaView mediaView;

    @FXML
    private BorderPane overlay;
    @FXML
    private VBox controls;
    @FXML
    private Slider progressBar;
    @FXML
    private Button btnPlay;
    @FXML
    private HBox leftControls, rightControls;
    @FXML
    private Label lblTime;
    @FXML
    private ToggleButton loopToggle;

    double maxHeight = Main.screenHeight * 0.75, maxWidth = Main.screenWidth * 0.75;
    Stage stage;

    int btnSize = 40;
    double graphicRatio = btnSize * 0.6;
    ImageView pauseImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/pause.png").toString()),
              playImg = new ImageView(getClass().getResource("/myself/projects/mygallery/images/play.png").toString());

    boolean wasPlaying, //used to determine whether to unpause after moving slider
            atEnd; //SHOULD be unnecessary... used as a temporary workaround for a weird issue

    public void init(ViewItem viewItem, Stage stage)
    {
        this.stage = stage;
        stage.setOnCloseRequest(e -> close());
        //adjusts play button graphics
        pauseImg.setFitHeight(graphicRatio);
        pauseImg.setFitWidth(graphicRatio);
        playImg.setFitHeight(graphicRatio);
        playImg.setFitWidth(graphicRatio);

        btnPlay.setGraphic(pauseImg);
        btnPlay.setPrefHeight(btnSize);
        btnPlay.setPrefWidth(btnSize);

        //prepares viewport according to media type
        Node node;

        if(MediaUtils.isImage(viewItem.getType()) || viewItem.getType().equals("gif"))
        {
            overlay.getChildren().remove(controls); //removes irrelevant media controls

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
            //binds image size to window size
            stage.setOnShown(e ->
            {
                imageView.fitHeightProperty().bind(stage.getScene().heightProperty());
                imageView.fitWidthProperty().bind(stage.getScene().widthProperty());
            });

            node = imageView;
        }
        else if(MediaUtils.isVideo(viewItem.getType()))
        {
            MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File(viewItem.getPath()).toURI().toString()));
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setOnReady(() ->
            {
                // - - - - constraining window size - - - - //

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

                double yComp = stage.getHeight() - stage.getScene().getHeight(), xComp = stage.getWidth() - stage.getScene().getWidth(); //compensation for size diff between stage and scene

                stage.setHeight(height + yComp);
                stage.setWidth(width + xComp);
                stage.centerOnScreen();

                // - - - - media control setup - - - - //

                //aligning media controls (extremely clunky...)
                double btnCenter = (stage.getScene().getWidth() - btnSize) / 2, btnMargins = 15;
                AnchorPane.setLeftAnchor(btnPlay, btnCenter);
                AnchorPane.setRightAnchor(leftControls, btnCenter + btnSize + btnMargins);
                AnchorPane.setLeftAnchor(rightControls, btnCenter + btnSize + btnMargins);
                stage.getScene().widthProperty().addListener((observable, oldValue, newValue) ->
                {
                    AnchorPane.setLeftAnchor(btnPlay, (newValue.doubleValue() - btnSize) / 2);
                    AnchorPane.setRightAnchor(leftControls, (newValue.doubleValue() + btnSize + btnMargins) / 2);
                    AnchorPane.setLeftAnchor(rightControls, (newValue.doubleValue() + btnSize + btnMargins) / 2);
                });

                //progressBar slider
                progressBar.setMax(mediaPlayer.getTotalDuration().toMillis());
                mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> progressBar.setValue(newValue.toMillis()));
                mediaPlayer.setOnEndOfMedia(() -> mediaEnd());

                //time signature label
                progressBar.valueProperty().addListener((observable, oldValue, newValue) ->
                {
                    int min = (int) (newValue.intValue() * 0.001 / 60);
                    int sec = (int) (newValue.intValue() * 0.001) % 60;
                    String signature = min + ":" + (sec < 10 ? "0" + sec : sec);
                    lblTime.setText(signature);
                });
            });

            node = mediaView;
        }
        else return;

        node.setDisable(false);
        node.setVisible(true);
    }

    @FXML //handles play button logic
    private void btnPlay()
    {
        if(isPlaying() && !atEnd)
            pause();
        else
        {
            if(atEnd) seek(0);
            play();
        }
    }

    //slider input methods
    @FXML
    private void sliderSeek() { seek(progressBar.getValue()); }
    @FXML
    private void sliderPressed()
    {
        wasPlaying = isPlaying();
        pause();
    }
    @FXML
    private void sliderReleased()
    {
        if(wasPlaying && !(progressBar.getValue() == progressBar.getMax() && !loopToggle.isSelected()))
        {
            play();
//            mediaEnd(); //i shouldn't really need this
        }
    }

    @FXML
    private void toggleLoop() { mediaView.getMediaPlayer().setCycleCount(loopToggle.isSelected() ? MediaPlayer.INDEFINITE : 0); }

    //mediaView utility methods
    private void seek(double millis)
    {
        mediaView.getMediaPlayer().seek(new Duration(millis));
        atEnd = millis > progressBar.getMax() - 100;
    }

    private void play()
    {
        mediaView.getMediaPlayer().play();
        btnPlay.setGraphic(pauseImg);
    }
    private void pause()
    {
        mediaView.getMediaPlayer().pause();
        btnPlay.setGraphic(playImg);
    }
    private boolean isPlaying() { return mediaView.getMediaPlayer().getStatus().equals(Status.PLAYING); } //unreliable at the end of playback

    private void mediaEnd()
    {
        System.out.println("end");
        if(!loopToggle.isSelected())
        {
            progressBar.setValue(progressBar.getMax());
            pause(); //mediaPlayer status doesn't change to PAUSED, so i need an extra boolean
            atEnd = true; //sigh...
        }
    }

    private void close()
    {
        //properly disposes media when closed
        if(!mediaView.isDisable())
            mediaView.getMediaPlayer().dispose();
    }
}