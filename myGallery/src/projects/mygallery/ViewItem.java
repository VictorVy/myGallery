package projects.mygallery;

public class ViewItem //class for media files
{
    private final String name;
    private final String path;

    public ViewItem(String name, String path)
    {
        this.name = name;
        this.path = path;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
}
