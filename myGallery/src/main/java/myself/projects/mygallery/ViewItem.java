package myself.projects.mygallery;

//class for media files
public class ViewItem
{
    private final String name, path, type, thumb, cDate;

    public ViewItem(String name, String type, String path, String cDate)
    {
        this.name = name;
        this.type = type.toLowerCase();
        this.path = path;
        this.cDate = cDate;
        thumb = MediaUtils.getUserDataDirectory() + name + (type.equals("gif") ? ".gif" : ".png");
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public String getType() { return type; }
    public String getThumb() { return thumb; }
    public String getCDate() { return cDate; }
}