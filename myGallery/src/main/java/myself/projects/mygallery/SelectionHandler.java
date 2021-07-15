package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

//handles selection in the gallery view
public class SelectionHandler
{
    private static ObservableList<ViewItem> selected = FXCollections.observableArrayList();

    public static void clicked(ViewItem viewItem, boolean shiftPressed, boolean controlPressed)
    {
        if(shiftPressed && selected.size() > 0)
        {
            ObservableList<ViewItem> viewItems = SQLConnector.getDBItems();
            ViewItem first = selected.get(0);
            selected.clear();
            selected.add(first);

            int a = ViewItem.indexOf(viewItems, first), b = ViewItem.indexOf(viewItems, viewItem);

            if(a != b) selected.addAll(a < b ? viewItems.subList(a + 1, b + 1) : viewItems.subList(b, a));
        }
        else if(controlPressed)
        {
            if(isSelected(viewItem))
                selected.remove(ViewItem.indexOf(selected, viewItem));
            else
                selected.add(viewItem);
        }
        else
        {
            selected.clear();
            selected.add(viewItem);
        }
    }

    public static boolean isSelected(ViewItem viewItem)
    {
        for(ViewItem vi : selected)
            if(vi.equals(viewItem)) return true;

        return false;
    }

    public static void deselectAll() { selected.clear(); }

    public static ObservableList<ViewItem> getSelected() { return selected; }
}
