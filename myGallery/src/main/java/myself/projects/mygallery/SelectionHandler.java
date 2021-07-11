package myself.projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

//handles selection in the gallery view
public class SelectionHandler
{
    private static ObservableList<ViewItem> selected = FXCollections.observableArrayList();

    public static void clicked(ViewItem viewItem)
    {
        selected.clear();
        selected.add(viewItem);
        System.out.println("ay");
    }

    public static ObservableList<ViewItem> getSelected() { return selected; }
}
