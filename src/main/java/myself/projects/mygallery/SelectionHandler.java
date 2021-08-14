package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseButton;

//handles selection in the gallery view
public class SelectionHandler
{
    private static ObservableList<ViewItem> selected = FXCollections.observableArrayList();

    public static void viewItemClicked(ViewItem viewItem, boolean shiftPressed, boolean controlPressed, MouseButton mouseButton, int clickCount)
    {
        //differentiate between mouse buttons
        if(mouseButton.equals(MouseButton.PRIMARY))
        {
            //differentiate between key combinations
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
                switch(clickCount)
                {
                    case 1:
                        selected.clear();
                        selected.add(viewItem);
                        break;
                    case 2:
                        MainController.showItem(viewItem);
                        break;
                }

            }
        }
    }

    public static void detailsClicked(ViewItem viewItem, int clickCount)
    {
        if(clickCount == 2)
            MainController.showItem(viewItem);
    }

    public static boolean isSelected(ViewItem viewItem)
    {
        for(ViewItem vi : selected)
            if(vi.equals(viewItem)) return true;

        return false;
    }

    public static void deselectAll() { selected.clear(); }

    public static ObservableList<ViewItem> getSelected() { return selected; }
    public static void setSelected(ObservableList<ViewItem> viewItems)
    {
        selected.clear();
        selected.addAll(viewItems);
    }
}
