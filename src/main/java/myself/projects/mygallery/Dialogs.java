package myself.projects.mygallery;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Dialogs
{
    //returns an alert to confirm item removal with user
    public static Alert createRemovalAlert(ObservableList<String> toRemove, String title, String content)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(String.valueOf(Dialogs.class.getResource("/myself/projects/mygallery/images/bin.png"))));
//        alert.initOwner(Main.stage);

        //setting expandable content
        Label itemNames = new Label(MiscUtils.toListString(toRemove));
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
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(String.valueOf(Dialogs.class.getResource("/myself/projects/mygallery/images/fileError.png"))));

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

    //returns a dialog for user text input
    public static TextInputDialog createTextInputDialog()
    {
        TextInputDialog dialog = new TextInputDialog();

        dialog.getDialogPane().setPrefWidth(250);
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image(String.valueOf(Dialogs.class.getResource("/myself/projects/mygallery/images/gallery.png"))));

        dialog.setTitle("Create Tags");
        dialog.setHeaderText("Enter tag name(s):");
        dialog.setResizable(true);
        dialog.setGraphic(null);

        return dialog;
    }
}
