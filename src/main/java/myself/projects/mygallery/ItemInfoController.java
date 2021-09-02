package myself.projects.mygallery;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ItemInfoController
{
    private ViewItem viewItem;

    @FXML
    private Label tagsLbl;

    public void init(ViewItem viewItem)
    {
        this.viewItem = viewItem;


    }

    @FXML
    private void attachTags()
    {


//        SQLConnector.insertXRef(new String[] {viewItem.getPath()},  );
    }
    @FXML
    private void removeTags()
    {

    }
}
