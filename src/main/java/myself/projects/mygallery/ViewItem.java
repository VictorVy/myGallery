package myself.projects.mygallery;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

//class for media files
public class ViewItem
{
    private final int id;
    private final String name, path, type, thumb, cDate, aDate;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public ViewItem(int id, String name, String type, String path, String cDate, String aDate)
    {
        this.id = id;
        this.name = name;
        this.type = type.toLowerCase();
        this.path = path;
        this.cDate = cDate;
        this.aDate = aDate;
        thumb = MiscUtils.getUserDataDirectory() + name + (type.equals("gif") ? ".gif" : ".png");
    }

    public static int indexOf(ObservableList<ViewItem> list, ViewItem vi)
    {
        for(int i = 0; i < list.size(); i++)
            if(list.get(i).equals(vi)) return i;

        return -1;
    }

    public static boolean contains(ObservableList<ViewItem> list, ViewItem vi)
    {
        for(ViewItem viewItem : list)
            if(viewItem.equals(vi)) return true;

        return false;
    }

    public boolean equals(ViewItem vi) { return getPath().equals(vi.getPath()); }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPath() { return path; }
    public String getType() { return type; }
    public String getThumb() { return thumb; }
    public String getCDate() { return cDate; }
    public String getADate() { return aDate; }
    public boolean isSelected() { return selected.get(); }
    public BooleanProperty getSelectedProperty() { return selected; }
    public void setSelected(boolean bool) { selected.setValue(bool); }
}