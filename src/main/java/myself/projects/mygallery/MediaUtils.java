package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
//import javafx.embed.swing.SwingFXUtils;
//import javafx.scene.image.WritableImage;
//import javafx.scene.media.Media;
//import javafx.scene.media.MediaView;
//import javafx.scene.media.MediaPlayer;
//import javafx.scene.effect.*;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.paint.Color;
import net.coobird.thumbnailator.Thumbnails;
import org.jcodec.api.JCodecException;
import org.jcodec.api.awt.AWTFrameGrab;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
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
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void createThumbs(ObservableList<ViewItem> viewItems, int sizeLimit)
    {
        try
        {
            for(ViewItem vi : viewItems)
            {
                if(MediaUtils.isImage(vi.getType()))
                    MediaUtils.createImageThumb(vi, sizeLimit);
                else if(vi.getType().equals("gif"))
                    MediaUtils.createGifThumb(vi, sizeLimit);
                else if(MediaUtils.isVideo(vi.getType()))
                    MediaUtils.createVideoThumb(vi, sizeLimit);
                else if(MediaUtils.isAudio(vi.getType()))
                    MediaUtils.createAudioThumb(vi);
            }
        }
        catch(JCodecException | IOException e) { e.printStackTrace(); }
    }

    //creates and saves an image thumbnail
    public static void createImageThumb(ViewItem vi, int sizeLimit) throws IOException
    {
        //saves thumbnail using thumbnailator
        //only resizes thumbnail if needed
        BufferedImage image = ImageIO.read(new File(vi.getPath()));
        double width = image.getWidth();
        double height = image.getHeight();

        if(width > sizeLimit || height > sizeLimit)
            width = height = sizeLimit;

        Thumbnails.of(image).size((int) width, (int) height)
                            .toFile(getUserDataDirectory() + vi.getName() + ".png");
    }

    //creates and saves a gif thumbnail (that's also a gif)
    public static void createGifThumb(ViewItem vi, int sizeLimit) throws IOException
    {
        //making map set of resized frames
        LinkedHashMap<BufferedImage, Integer> frames = decodeGif(vi.getPath());
        LinkedHashMap<BufferedImage, Integer> newFrames = new LinkedHashMap<>();

        Set<Map.Entry<BufferedImage, Integer>> entrySet = frames.entrySet();

        for(Map.Entry<BufferedImage, Integer> entry : entrySet)
            newFrames.put(Thumbnails.of(entry.getKey()).size(sizeLimit, sizeLimit).asBufferedImage(), entry.getValue());

        encodeGif(newFrames,getUserDataDirectory() + vi.getName() + ".gif");
    }

    //returns a map of all the frames and delay times
    private static LinkedHashMap<BufferedImage, Integer> decodeGif(String path)
    {
        GifDecoder decoder = new GifDecoder();
        decoder.read(path);
        int frameCount = decoder.getFrameCount();

        LinkedHashMap<BufferedImage, Integer> frames = new LinkedHashMap<>(frameCount);

        for(int i = 0; i < frameCount; i++)
        {
            BufferedImage frame = decoder.getFrame(i);
            int delay = decoder.getDelay(i);

            frames.put(frame, delay);
        }

        return frames;
    }

    //writes a gif file to the data directory
    private static void encodeGif(LinkedHashMap<BufferedImage, Integer> frames, String outPath)
    {
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(outPath);
        encoder.setRepeat(0);
        encoder.setQuality(1);

        //adding frames to gif
        Set<Map.Entry<BufferedImage, Integer>> entrySet = frames.entrySet();

        for(Map.Entry<BufferedImage, Integer> entry : entrySet)
        {
            encoder.setDelay(entry.getValue());
            encoder.addFrame(entry.getKey());
        }

        encoder.finish();
    }

    //creates a video thumbnail
    private static void createVideoThumb(ViewItem vi, int sizeLimit) throws IOException, JCodecException
    {
        BufferedImage thumb = AWTFrameGrab.getFrame(new File(vi.getPath()), 0);

        double width = thumb.getWidth();
        double height = thumb.getHeight();

        if(width > sizeLimit || height > sizeLimit)
            width = height = sizeLimit;

        Thumbnails.of(thumb).size((int) width, (int) height)
                            .toFile(new File(getUserDataDirectory() + vi.getName() + ".png"));
    }

    //method without external libraries is inconsistent and barely works
//    private static void createVideoThumb()
//    {
//        MediaPlayer mp = new MediaPlayer(new Media(new File([path here]).toURI().toString()));
//
//        mp.setOnReady(() ->
//        {
//            MediaView mv = new MediaView();
//            mv.setMediaPlayer(mp);
//
//            WritableImage wi = mv.snapshot(null, null);
//
//            double width = wi.getWidth();
//            double height = wi.getHeight();
//            System.out.println(width + " " + height);
//            if(width > 150 || height > 150)
//            {
//                width = 150;
//                height = 150;
//            }
//
//            //writes thumbnail to data folder
//            try
//            {
//                Thumbnails.of(SwingFXUtils.fromFXImage(wi, null))
//                        .size((int) width, (int) height)
//                        .toFile("C:/Users/victo/Desktop/test.png");
//            }
//            catch(Exception e) { e.printStackTrace(); }
//        });
//    }

    private static void createAudioThumb(ViewItem vi) throws IOException
    {
        ImageIO.write(ImageIO.read(Objects.requireNonNull(MediaUtils.class.getResource("/myself/projects/mygallery/images/music.png"))),
            "png",
                       new File(getUserDataDirectory() + vi.getName() + ".png"));
    }

    public static void removeThumbs(ObservableList<ViewItem> selectedItems)
    {
        for(ViewItem vi : selectedItems)
            new File(getUserDataDirectory() + vi.getName() + (vi.getType().equals("gif") ? "gif" : "png")).delete();
    }

    //PROPERLY gets the folder where the thumbnails are stored
    public static String getUserDataDirectory() { return System.getProperty("user.home") + File.separator + "myGallery" + File.separator + "thumbs" + File.separator; }

    //returns a list of ViewItems from a list of Files
    public static ObservableList<ViewItem> filesToViewItems(List<File> files)
    {
        ObservableList<ViewItem> viewItems = FXCollections.observableArrayList();

        for(File f : files)
        {
            try
            {
                BasicFileAttributes bfa = Files.readAttributes(f.toPath(), BasicFileAttributes.class);

                viewItems.add(new ViewItem(f.getName().substring(0, f.getName().indexOf('.')), f.getName().substring(f.getName().lastIndexOf('.') + 1), f.getAbsolutePath(), bfa.creationTime().toString(), LocalDateTime.now().toString()));
            }
            catch(IOException e) { e.printStackTrace(); }
        }

        return viewItems;
    }

    //returns a list of incorrectly-typed items
    public static ObservableList<ViewItem> wrongFiles(List<File> files)
    {
        ObservableList<ViewItem> items = filesToViewItems(files);
        ObservableList<ViewItem> notMedia = FXCollections.observableArrayList();

        for(ViewItem vi : items)
            if(!isImage(vi.getType()) && !vi.getType().equals("gif") && !isVideo(vi.getType()) && !isAudio(vi.getType())) notMedia.add(vi);

        return notMedia;
    }

    //check item type
    public static Boolean isImage(String type) { return type.equals("png") || type.equals("jpg") || type.equals("bmp"); }
    public static Boolean isVideo(String type) { return type.equals("mp4") || type.equals("m4v") || type.equals("flv"); }
    public static Boolean isAudio(String type) { return type.equals("mp3") || type.equals("wav")|| type.equals("aif") || type.equals("aiff"); }

//    public static InnerShadow hoverEffect() //disused
//    {
//        InnerShadow effect = new InnerShadow();
//
//        effect.setBlurType(BlurType.GAUSSIAN);
//        effect.setColor(new Color(0, 0.663, 0.969, 0.8));
//        effect.setWidth(50);
//        effect.setHeight(50);
//        effect.setRadius(42);
//
//        return effect;
//    }

    public static String millisToStamp(int millis)
    {
        int min = (int) (millis * 0.001 / 60);
        int sec = (int) (millis * 0.001) % 60;

        return min + ":" + (sec < 10 ? "0" + sec : sec);
    }
}