package myself.projects.mygallery;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Alerts
{
    //returns an alert to confirm file removal with user
    public static Alert createFileRemovalAlert(ObservableList<ViewItem> viewItems)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Removal");
        alert.setHeaderText(null);
        alert.setContentText("Remove " + viewItems.size() + " item(s)?");
        alert.setResizable(false);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Alerts.class.getResource("/myself/projects/mygallery/images/bin.png").toString()));
//        alert.initOwner(Main.stage);

        //creating and setting expandable content
        StringBuilder names = new StringBuilder("Items: \n");

        String prefix = "";
        for(ViewItem vi : viewItems)
        {
            names.append(prefix);
            prefix = ", ";
            names.append(vi.getName() + '.' + vi.getType());
        }

        Label itemNames = new Label(names.toString() + '.');
        itemNames.setWrapText(true);

        alert.getDialogPane().setExpandableContent(itemNames);
        alert.getDialogPane().setPrefWidth(0);

        return alert;
    }

    //returns an error alert when user drags non-media file
    public static Alert createDragAlert(ObservableList<ViewItem> notMedia)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Incorrect Filetype");
        alert.setHeaderText(null);
        alert.setContentText("Unsupported file format!");
        alert.setResizable(false);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Alerts.class.getResource("/myself/projects/mygallery/images/bin.png").toString()));

        //creating and setting expandable content
        StringBuilder names = new StringBuilder("Items: \n");

        String prefix = "";
        for(ViewItem vi : notMedia)
        {
            names.append(prefix);
            prefix = ", ";
            names.append(vi.getName() + '.' + vi.getType());
        }

        Label itemNames = new Label(names.toString() + '.');
        itemNames.setWrapText(true);

        alert.getDialogPane().setExpandableContent(itemNames);
        alert.getDialogPane().setPrefWidth(0);

        return alert;
    }

    //returns an alert to confirm tag deletion with user
    public static Alert createTagDeletionAlert(ObservableList<String> tags)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Removal");
        alert.setHeaderText(null);
        alert.setContentText("Remove " + tags.size() + " tag(s)?");
        alert.setResizable(false);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Alerts.class.getResource("/myself/projects/mygallery/images/bin.png").toString()));
//        alert.initOwner(Main.stage);

        //creating and setting expandable content
        StringBuilder names = new StringBuilder("Items: \n");

        String prefix = "";
        for(String t : tags)
        {
            names.append(prefix);
            prefix = ", ";
            names.append(t);
        }

        Label itemNames = new Label(names.toString() + '.');
        itemNames.setWrapText(true);

        alert.getDialogPane().setExpandableContent(itemNames);
        alert.getDialogPane().setPrefWidth(0);

        return alert;
    }
}
