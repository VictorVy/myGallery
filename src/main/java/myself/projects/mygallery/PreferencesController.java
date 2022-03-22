package myself.projects.mygallery;

import javafx.stage.Stage;

public class PreferencesController
{
    private Stage stage;

    public void init(Stage stage)
    {
        this.stage = stage;
    }

    public void show() { stage.show(); }
}
