package myself.projects.mygallery;

//class for media files
public class ViewItem
{
    private final String name, path, type;
//    private Image thumb;

    public ViewItem(String name, String path)
    {
        this.name = name;
        this.path = path;
        type = name.substring(name.indexOf('.') + 1).toLowerCase();

//        if(MediaUtils.isImage(type))
//            thumb = MediaUtils.imageThumb(path, 150);
//        else if(MediaUtils.isVideo(type))
//            thumb = MediaUtils.videoThumb(path, 150);
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public String getType() { return type; }
//    public Image getThumb() { return thumb; }
}