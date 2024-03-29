package myself.projects.mygallery;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
    private ViewItem viewItem;

    @FXML
    private BorderPane borderPane;

    @FXML
    private ImageView imageView;
    @FXML
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;

    @FXML
    private BorderPane overlay;
    @FXML
    private VBox controls;
    @FXML
    private Slider progressBar, volumeSlider, rateSlider;
    @FXML
    private Button btnPlay, btnFullScreen;
    @FXML
    private HBox leftControls, rightControls, rCornerControls;
    @FXML
    private Label lblTime, lblRate;
    @FXML
    private ToggleButton loopToggle, muteToggle;
    @FXML
    private MenuButton rateMenuBtn;
    @FXML
    private CheckBox expandCheckBox;

    @FXML
    private Tooltip progressTooltip, playTooltip, volumeTooltip, muteTooltip;

    ImageView pauseImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/pause.png"))),
              playImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/play.png"))),
              loopImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/loop.png"))),
              volMuteImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/volMute.png"))),
              volLImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/volL.png"))),
              volMImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/volM.png"))),
              volHImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/volH.png"))),
              rateImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/rate.png"))),
              fullScreenImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/fullScreen.png"))),
              normalScreenImg = new ImageView(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/normalScreen.png")));

    double maxHeight = Main.screenHeight * 0.75, maxWidth = Main.screenWidth * 0.75;
    Stage stage;

    int btnSize = 40;
    double graphicRatio = btnSize * 0.6;
    double audioHeight = Main.screenHeight * 0.5, audioWidth = Main.screenWidth * 0.5;

    boolean wasPlaying;

    int fadeOutTime = 500, fadeInTime = 100;
    FadeTransition fadeOutControls = new FadeTransition(),
                   fadeInControls = new FadeTransition();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;
    private static final long fadeDelay = 500;

    public void init(ViewItem viewItem, Stage stage)
    {
        this.viewItem = viewItem;
        this.stage = stage;
        stage.setOnCloseRequest(e ->
        {
            close();
            Main.allScenes.remove(stage.getScene().getRoot());
        });
        stage.setFullScreenExitHint("");
        borderPane.setCursor(Cursor.DEFAULT);

        stage.getScene().setOnKeyPressed(this::keyPressed); //doesn't work in scene builder for some reason...

        //prepares fade transitions
        fadeOutControls.setNode(overlay);
        fadeInControls.setNode(overlay);
        fadeOutControls.setToValue(0);
        fadeInControls.setToValue(1);
        //transition easing (i wonder why there isn't a simple ease() function for animations)
        fadeOutControls.setInterpolator(new Interpolator() { @Override protected double curve(double t) { return Math.pow(t, 2); }});
        //sets fade transition listeners
        borderPane.setOnMouseMoved(e ->
        {
            if(overlay.getOpacity() != 1) fadeInControls();
            //set fade scheduler
            if(future != null) future.cancel(false);
            future = executor.schedule(() -> { if(!controls.isHover()) fadeOutControls(); }, fadeDelay, TimeUnit.MILLISECONDS);
        });
        borderPane.getScene().setOnMouseExited(e ->
        {
            if(overlay.getOpacity() != 0 && !rateMenuBtn.isShowing()) fadeOutControls();
            if(future != null) future.cancel(false);
        });

        //prepares viewport according to media type
        Node node;
        if(MiscUtils.isImage(viewItem.getType()) || viewItem.getType().equals("gif"))
        {
            overlay.getChildren().remove(controls); //removes irrelevant media controls
            imageViewInit(new Image("file:" + viewItem.getPath()));
            node = imageView;
        }
        else if(MiscUtils.isVideo(viewItem.getType()))
        {
            mediaViewInit();
            node = mediaView;
        }
        else if(MiscUtils.isAudio(viewItem.getType()))
        {
            imageViewInit(new Image(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/music.png"))));
            audioPlayerInit();
            node = imageView;
        }
        else
        {
            System.out.println("file type error");
            return;
        }
        //selectively enables appropriate node
        node.setDisable(false);
        node.setVisible(true);

        Main.allScenes.add(stage.getScene().getRoot());
        MiscUtils.updateStyles();
    }

    private void imageViewInit(Image image)
    {
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
    }

    private void mediaViewInit()
    {
        mediaPlayerInit();

        mediaPlayer.setOnReady(() ->
        {
            //handles node + window sizing
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

            mediaView.setMediaPlayer(mediaPlayer);
            //prepares rest of media controls
            mediaControlsInit();
        });
    }

    private void mediaPlayerInit()
    {
        btnPlay.setPrefHeight(btnSize);
        btnPlay.setPrefWidth(btnSize);
        stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> btnFullScreen.setGraphic(newValue ? normalScreenImg: fullScreenImg));

        mediaPlayer = new MediaPlayer(new Media(new File(viewItem.getPath()).toURI().toString()));
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setOnPlaying(() ->
        {
            btnPlay.setGraphic(pauseImg);
            playTooltip.setText("Pause");
        });
        mediaPlayer.setOnPaused(() ->
        {
            btnPlay.setGraphic(playImg);
            playTooltip.setText("Play");
            progressBar.valueProperty().removeListener(workaround); //inefficient but necessary as part of workaround
        });
        mediaPlayer.setOnEndOfMedia(() ->
        {
            if(!loopToggle.isSelected())
            {
                progressBar.setValue(progressBar.getMax());
                btnPlay.setGraphic(playImg);
            }
        });
    }

    private void mediaControlsInit()
    {
        //fixing fade bug regarding menu button
        rateMenuBtn.showingProperty().addListener((observable, oldValue, newValue) -> { if(oldValue && !overlay.isHover()) fadeOutControls(); });

        //aligning media controls (extremely primitive...)
        double btnCenter = (stage.getScene().getWidth() - btnSize) / 2, btnMargins = 20;

        AnchorPane.setLeftAnchor(btnPlay, btnCenter);
        AnchorPane.setRightAnchor(leftControls, btnCenter + btnSize + btnMargins);
        AnchorPane.setLeftAnchor(rightControls, btnCenter + btnSize + btnMargins);
        AnchorPane.setRightAnchor(rCornerControls, btnMargins);

        stage.getScene().widthProperty().addListener((observable, oldValue, newValue) ->
        {
            AnchorPane.setLeftAnchor(btnPlay, (newValue.doubleValue() - btnSize) / 2);
            AnchorPane.setRightAnchor(leftControls, (newValue.doubleValue() + btnSize + btnMargins) / 2);
            AnchorPane.setLeftAnchor(rightControls, (newValue.doubleValue() + btnSize + btnMargins) / 2);
        });

        //btnPlay graphics
        pauseImg.setFitHeight(graphicRatio);
        pauseImg.setFitWidth(graphicRatio);
        playImg.setFitHeight(graphicRatio);
        playImg.setFitWidth(graphicRatio);
        btnPlay.setGraphic(pauseImg);
        //loopToggle graphic
        loopImg.setFitHeight(graphicRatio);
        loopImg.setFitWidth(graphicRatio);
        loopToggle.setPadding(new Insets(3, 4, 3, 4));
        loopToggle.setGraphic(loopImg);
        //muteToggle graphics
        volMuteImg.setFitHeight(graphicRatio);
        volMuteImg.setFitWidth(graphicRatio);
        volLImg.setFitHeight(graphicRatio);
        volLImg.setFitWidth(graphicRatio);
        volMImg.setFitHeight(graphicRatio);
        volMImg.setFitWidth(graphicRatio);
        volHImg.setFitHeight(graphicRatio);
        volHImg.setFitWidth(graphicRatio);
        muteToggle.setPadding(new Insets(4, 6, 4, 7));
        muteToggle.setGraphic(volHImg);
        //rateButton graphics
        rateImg.setFitHeight(btnSize * 0.5);
        rateImg.setFitWidth(btnSize * 0.5);
        rateMenuBtn.setGraphic(rateImg);
        //btnFullScreen graphics
        fullScreenImg.setFitHeight(graphicRatio);
        fullScreenImg.setFitWidth(graphicRatio);
        normalScreenImg.setFitHeight(graphicRatio);
        normalScreenImg.setFitWidth(graphicRatio);
//        btnFullScreen.setPadding(new Insets(3, 4, 3, 4)); //TODO: fix padding
        btnFullScreen.setGraphic(fullScreenImg);

        //progressBar slider
        progressBar.setMax(mediaPlayer.getTotalDuration().toMillis());
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> progressBar.setValue(newValue.toMillis()));
        //progressBar listener for timestamp + tooltip
        progressBar.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            String timestamp = MiscUtils.millisToStamp(newValue.intValue()) + " / " + MiscUtils.millisToStamp((int) progressBar.getMax());
            lblTime.setText(timestamp);
            progressTooltip.setText(timestamp.substring(0, timestamp.indexOf(' ')));
        });

        //volume slider
        mediaPlayer.muteProperty().addListener((observable, oldValue, newValue) ->
        {
            muteToggle.setGraphic(newValue ? volMuteImg : getVolumeImage());
            muteToggle.setSelected(newValue); //sometimes redundant
        });
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            if(mediaPlayer.isMute()) mediaPlayer.setMute(false);
            mediaPlayer.setVolume(newValue.doubleValue());
            muteToggle.setGraphic(getVolumeImage());
        });
        volumeSlider.setOnMouseMoved(e -> volumeTooltip.setText((int)(e.getX() / volumeSlider.getMax()) + ""));

        //rate slider + label
        mediaPlayer.rateProperty().addListener((observable, oldValue, newValue) -> lblRate.setText(Math.round(newValue.doubleValue() * 4) / 4.0 + ""));
        rateSlider.valueProperty().addListener((observable, oldValue, newValue) -> { if(newValue.doubleValue() > 0.01) mediaPlayer.setRate(newValue.doubleValue()); });
    }

    private void audioPlayerInit()
    {
        mediaPlayerInit();

        //handles window sizing
        stage.setOnShown(event ->
        {
            stage.setHeight(audioHeight);
            stage.setWidth(audioWidth);
            stage.centerOnScreen();
        });

        //prepares rest of media controls
        mediaPlayer.setOnReady(this::mediaControlsInit);
    }

    @FXML
    private void windowClicked(MouseEvent e)
    {
        if(e.getClickCount() == 2)
            stage.setFullScreen(!stage.isFullScreen());
        else //if(!controls.isHover())
        {
            if(overlay.getOpacity() != 1) fadeInControls();
            else if(overlay.getOpacity() != 0) fadeOutControls();
        }
    }

    private void keyPressed(KeyEvent e)
    {
        KeyCode code = e.getCode();

        if(code.equals(KeyCode.LEFT) || code.equals(KeyCode.RIGHT))
        {
            ObservableList<ViewItem> list = Main.mainController.getViewItems();

            if(list.size() == 0) return;

            for(int i = 0; i < list.size(); i++)
            {
                if(viewItem.equals(list.get(i)))
                {
                    if(code.equals(KeyCode.LEFT))
                        switchVI(i == 0 ? list.get(list.size() - 1) : list.get(i - 1));
                    else
                        switchVI(i == list.size() - 1 ? list.get(0) : list.get(i + 1));
                }
            }
        }
    }

    private void switchVI(ViewItem viewItem)
    {
        try //necessary?
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/myself/projects/mygallery/view-window.fxml"));

            Stage stage = new Stage();
            stage.setTitle(viewItem.getName() + "." + viewItem.getType());
            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/myself/projects/mygallery/images/gallery.png"))));

            Scene scene = new Scene(loader.load());
            scene.getRoot().getStylesheets().add(Main.stylesheet_dark);

            stage.setScene(scene);

            ViewWindowController viewWindowController = loader.getController();
            viewWindowController.init(viewItem, stage);

            stage.setMaximized(this.stage.isMaximized());
            stage.setFullScreen(this.stage.isFullScreen());

            stage.show();
        }
        catch(IOException e) { e.printStackTrace(); }

        close();
        stage.close();
    }

    @FXML
    private void btnPlay()
    {
        if(mediaAtEnd() || progressBarAtEnd()) //workaround for irrational media status at end
        {
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.play();
            btnPlay.setGraphic(pauseImg);
            return;
        }

        if(isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.play();
    }
    @FXML
    private void btnFullScreen() { stage.setFullScreen(!stage.isFullScreen()); }

    @FXML
    private void sliderClicked() { seek(progressBar.getValue()); }
    @FXML
    private void sliderDragged() { seek(progressBar.getValue()); }

    @FXML
    private void sliderPressed()
    {
        mediaPlayer.pause();
        wasPlaying = isPlaying() && !mediaAtEnd();

        if(mediaAtEnd()) //kludgy workaround due to finicky lack of pausability at end of media
        {
            progressBar.valueProperty().removeListener(workaround);
            progressBar.valueProperty().addListener(workaround);
        }
    }
    @FXML
    private void sliderReleased()
    {
        if(wasPlaying)
        {
            if(loopToggle.isSelected() && progressBarAtEnd()) mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.play();
        }
    }

    @FXML
    private void toggleLoop() { mediaPlayer.setCycleCount(loopToggle.isSelected() ? MediaPlayer.INDEFINITE : 0); }
    @FXML
    private void toggleMute()
    {
        mediaPlayer.setMute(muteToggle.isSelected());
        muteTooltip.setText(muteToggle.isSelected() ? "Unmute" : "Mute");
    }

    @FXML
    private void rateExpand()
    {
        rateSlider.setMax(expandCheckBox.isSelected() ? 8 : 2);
        rateSlider.setPrefWidth(expandCheckBox.isSelected() ? 250 : 125);
        rateSlider.setMajorTickUnit(expandCheckBox.isSelected() ? 1 : 0.5);
    }

    //mediaView utility methods
    private void seek(double millis) { mediaPlayer.seek(Duration.millis(millis)); }

    private boolean progressBarAtEnd() { return progressBar.getValue() == progressBar.getMax(); }
    private boolean mediaAtEnd() { return mediaPlayer.getCurrentTime().equals(mediaPlayer.getStopTime()); }
    private boolean isPlaying() { return mediaPlayer.getStatus().equals(Status.PLAYING); } //unreliable at the end of playback

    private void close()
    {
        //properly disposes of media when closed
        if(overlay.getChildren().contains(controls))
        {
            mediaPlayer.dispose();
//            System.out.println("hm");
        }
    }

    private ImageView getVolumeImage()
    {
        double millis = volumeSlider.getValue();
        if(millis == 0) return volMuteImg;
        if(millis < 1.0 / 3) return volLImg;
        if(millis < 2.0 / 3) return volMImg;
        return volHImg;
    }

    private void fadeOutControls()
    {
        fadeInControls.stop();
        fadeOutControls.setDuration(Duration.millis(fadeOutTime * overlay.getOpacity()));
        fadeOutControls.play();
        borderPane.setCursor(Cursor.NONE);
    }
    private void fadeInControls()
    {
        fadeOutControls.stop();
        fadeInControls.setDuration(Duration.millis(fadeInTime - fadeInTime * overlay.getOpacity()));
        fadeInControls.play();
        borderPane.setCursor(Cursor.DEFAULT);
    }

    ChangeListener<Number> workaround = (observable, oldValue, newValue) -> mediaPlayer.pause(); //listener used in workaround
}