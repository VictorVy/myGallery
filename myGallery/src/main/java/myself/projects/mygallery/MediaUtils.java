package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaPlayer;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class MediaUtils
{
    public static void initialize()
    {
        //creating folder to store thumbnails
        if(!Files.exists(Paths.get(getUserDataDirectory())))
        {
            try
            {
                Files.createDirectory(Paths.get(System.getProperty("user.home") + File.separator + "myGallery" + File.separator));
                Files.createDirectory(Paths.get(getUserDataDirectory()));
            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static void createThumbs(ObservableList<ViewItem> viewItems, int sizeLimit)
    {
        for(ViewItem vi : viewItems)
        {
            if(MediaUtils.isImage(vi.getType()))
                MediaUtils.createImageThumb(vi, sizeLimit);
            else if(vi.getType().equals("gif"))
                MediaUtils.createGifThumb(vi, sizeLimit);
            else if(MediaUtils.isVideo(vi.getType()))
                MediaUtils.createVideoThumb(vi, sizeLimit);
        }
    }

    //creates an image thumbnail
    public static void createImageThumb(ViewItem vi, int sizeLimit)
    {
        //saves thumbnail using thumbnailator
        try
        {
            //only resize thumbnail if needed
            BufferedImage image = ImageIO.read(new File(vi.getPath()));
            double width = image.getWidth();
            double height = image.getHeight();

            if(image.getWidth() > sizeLimit || image.getHeight() > sizeLimit)
            {
                width = sizeLimit;
                height = sizeLimit;
            }

            Thumbnails.of(image)
                    .size((int) width, (int) height)
                    .toFile(getUserDataDirectory() + vi.getName().replace(vi.getType(), "png"));
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    //creates a gif thumbnail (that's also a gif)
    public static void createGifThumb(ViewItem vi, int sizeLimit)
    {
        //making map set of resized frames
        LinkedHashMap<BufferedImage, Integer> frames = decodeGif(vi.getPath());
        LinkedHashMap<BufferedImage, Integer> newFrames = new LinkedHashMap<>();

        Set<Map.Entry<BufferedImage, Integer>> entrySet = frames.entrySet();

        for (Map.Entry<BufferedImage, Integer> entry : entrySet)
        {
            try
            {
                newFrames.put(Thumbnails.of(entry.getKey()).size(sizeLimit, sizeLimit).asBufferedImage(), entry.getValue());
            }
            catch (IOException e) { e.printStackTrace(); }
        }

        try
        {
            encodeGif(newFrames, vi.getPath(),getUserDataDirectory() + vi.getName());
        }
        catch (IOException e) { e.printStackTrace(); }
    }
    //returns a map of all the frames and delay times
    private static LinkedHashMap<BufferedImage, Integer> decodeGif(String path)
    {
        GifDecoder d = new GifDecoder();
        d.read(path);
        int n = d.getFrameCount();

        LinkedHashMap<BufferedImage, Integer> frames = new LinkedHashMap<>(n);

        for (int i = 0; i < n; i++)
        {
            BufferedImage frame = d.getFrame(i);
            int delay = d.getDelay(i);

            frames.put(frame, delay);
        }

        return frames;
    }
    //writes a gif file to the data directory
    private static void encodeGif(LinkedHashMap<BufferedImage, Integer> frames, String srcPath, String outPath) throws IOException
    {
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(outPath);
        encoder.setRepeat(0);
        encoder.setQuality(1);
        //this code to keep the transparency doesn't work!
//        Color transparentColor = null;
//        BufferedImage firstFrameImage = ImageIO.read(new File(srcPath));
//        if (firstFrameImage.getColorModel() instanceof IndexColorModel)
//        {
//            IndexColorModel cm = (IndexColorModel) firstFrameImage.getColorModel();
//            int transparentPixel = cm.getTransparentPixel();
//            transparentColor = new Color(cm.getRGB(transparentPixel), true);
//        }
//        encoder.setTransparent(transparentColor);

        //adding frames to gif
        Set<Map.Entry<BufferedImage, Integer>> entrySet = frames.entrySet();

        for (Map.Entry<BufferedImage, Integer> entry : entrySet)
        {
            encoder.setDelay(entry.getValue());
            encoder.addFrame(entry.getKey());
        }

        encoder.finish();
    }

    //creates a video thumbnail
    public static void createVideoThumb(ViewItem vi, int sizeLimit)
    {
        String name = vi.getName().substring(0, vi.getName().lastIndexOf('.')) + ".png";
        MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File(vi.getPath()).toURI().toString()));
        mediaPlayer.setOnReady(() -> vidThumbHelper(mediaPlayer, name, sizeLimit));
    }
    private static void vidThumbHelper(MediaPlayer mediaPlayer, String name, int sizeLimit)
    {
        double width = mediaPlayer.getMedia().getWidth();
        double height = mediaPlayer.getMedia().getHeight();

        MediaView mv = new MediaView();
        mv.setPreserveRatio(true);

        //resizes MediaView for snapshot
        if (width >= height && width > sizeLimit)
        {
            height = (sizeLimit * height) / width;
            width = sizeLimit;
        }
        else if (height > width && height > sizeLimit)
        {
            width = (sizeLimit * width) / height;
            height = sizeLimit;
        }

        mv.setFitWidth(width);
        mv.setFitHeight(height);
        mv.setMediaPlayer(mediaPlayer);

        WritableImage wi = new WritableImage((int) width, (int) height);
        wi = mv.snapshot(null, wi);

        //writes thumbnail to data folder
        try
        {
            Thumbnails.of(SwingFXUtils.fromFXImage(wi, null))
                    .scale(1)
                    .toFile(getUserDataDirectory() + name);
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public static void removeThumbs(ObservableList<ViewItem> selectedItems)
    {
        for(ViewItem vi : selectedItems)
            new File(getUserDataDirectory() + vi.getName().replace(vi.getType(), vi.getType().equals("gif") ? "gif" : "png")).delete();
    }

    //gets the folder where the thumbnails are stored
    public static String getUserDataDirectory() { return System.getProperty("user.home") + File.separator + "myGallery" + File.separator + "thumbs" + File.separator; }

    //returns a list of ViewItems from a list of Files
    public static ObservableList<ViewItem> filesToViewItems(List<File> files)
    {
        ObservableList<ViewItem> viewItems = FXCollections.observableArrayList();

        for (File f : files)
            viewItems.add(new ViewItem(f.getName(), f.getAbsolutePath()));

        return viewItems;
    }

    //returns a list of incorrectly-typed items
    public static ObservableList<ViewItem> wrongFiles(List<File> files)
    {
        ObservableList<ViewItem> items = filesToViewItems(files);
        ObservableList<ViewItem> notMedia = FXCollections.observableArrayList();

        for (ViewItem vi : items)
            if(!isImage(vi.getType()) && !vi.getType().equals("gif") && !isVideo(vi.getType())) notMedia.add(vi);

        return notMedia;
    }

    //check if file is image or video
    public static Boolean isImage(String type) { return type.equals("png") || type.equals("jpg") || type.equals("bmp"); }
    public static Boolean isVideo(String type) { return type.equals("mp4") || type.equals("m4v") || type.equals("flv") || type.equals("aif") || type.equals("aiff"); }
}