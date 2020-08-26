package myself.projects.mygallery;

//class for media files
public class ViewItem
{
    private final String name, path, type, thumb;

    public ViewItem(String name, String path)
    {
        this.name = name;
        this.path = path;
        type = name.substring(name.indexOf('.') + 1).toLowerCase();
        thumb = MediaUtils.getUserDataDirectory() + name.replace(type, type.equals("gif") ? "gif" : "png");
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public String getType() { return type; }
    public String getThumb() { return thumb; }
}