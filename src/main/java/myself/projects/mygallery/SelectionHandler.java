package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.List;

//handles selection in the gallery view
public class SelectionHandler
{
    private static final ObservableList<ViewItem> selected = FXCollections.observableArrayList();

    public static void initialize()
    {
        selected.addListener((ListChangeListener<ViewItem>) c ->
        {
            while(c.next())
            {
                if(c.wasAdded())
                    for(ViewItem viewItem : c.getAddedSubList())
                        viewItem.setSelected(true);

                if(c.wasRemoved())
                    for(ViewItem viewItem : c.getRemoved())
                        viewItem.setSelected(false);
            }
        });
    }

    public static boolean isSelected(ViewItem viewItem) { return ViewItem.contains(selected, viewItem); }

    public static void select(ViewItem viewItem) { selected.add(viewItem); }
    public static void select(List<ViewItem> items) { selected.addAll(items); }
    public static void deselect(ViewItem viewItem) { selected.remove(ViewItem.indexOf(selected, viewItem));}

    public static void selectAll()
    {
        clearSelection();
        select(Main.mainController.getViewItems());
    }
    public static void clearSelection() { selected.clear(); }

    public static ObservableList<ViewItem> getSelected() { return selected; }
    public static void setSelected(ViewItem viewItem)
    {
        clearSelection();
        select(viewItem);
    }
    public static void findAndSelect(ObservableList<ViewItem> viewItems)
    {
        clearSelection();

        ObservableList<ViewItem> items = Main.mainController.getViewItems();

        for(ViewItem vi : viewItems)
            select(items.get(ViewItem.indexOf(items, vi)));
    }
}
