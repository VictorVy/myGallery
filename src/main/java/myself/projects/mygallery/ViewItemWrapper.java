package myself.projects.mygallery;

import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class ViewItemWrapper extends StackPane
{
    private final ViewItem viewItem;

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    public ViewItemWrapper(ViewItem vi)
    {
        viewItem = vi;

        ImageView imageView = new ImageView("file:" + viewItem.getThumb());
        getChildren().add(imageView);
        setAlignment(Pos.CENTER);
        setOnMouseClicked(this::mouseClicked);

        getStyleClass().add("view-item-wrapper");

        viewItem.getSelectedProperty().addListener((observable, oldValue, newValue) -> pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, newValue));

        hoverProperty().addListener((observable, oldValue, newValue) -> Main.mainController.galleryHover = newValue);

        Tooltip.install(this, new Tooltip(viewItem.getName() + "." + viewItem.getType()));
    }

    private void mouseClicked(MouseEvent e)
    {
        //differentiate between mouse buttons
        if(e.getButton().equals(MouseButton.PRIMARY))
        {
            //differentiate between key modifiers
            if(e.isShiftDown() && SelectionHandler.getSelected().size() > 0) //shift-select
            {
                ObservableList<ViewItem> viewItems = Main.mainController.getViewItems();
                ViewItem first = SelectionHandler.getSelected().get(0);
                SelectionHandler.setSelected(first);

                int a = ViewItem.indexOf(viewItems, first), b = ViewItem.indexOf(viewItems, viewItem);

                if(a != b) SelectionHandler.select(a < b ? viewItems.subList(a + 1, b + 1) : viewItems.subList(b, a));
            }
            else if(e.isControlDown()) //control-select
            {
                if(SelectionHandler.isSelected(viewItem))
                    SelectionHandler.deselect(viewItem);
                else
                    SelectionHandler.select(viewItem);
            }
            else //no modifiers
            {
                switch(e.getClickCount())
                {
                    case 1:
                        SelectionHandler.setSelected(viewItem);
                        break;
                    case 2:
                        Main.mainController.showItem(viewItem);
                        break;
                }
            }
        }
        else if(e.getButton().equals(MouseButton.SECONDARY))
        {
            if(!viewItem.isSelected())
                SelectionHandler.setSelected(viewItem);
        }
    }

    public ViewItem getViewItem() { return viewItem; }
}
