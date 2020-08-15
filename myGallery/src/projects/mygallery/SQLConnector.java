package projects.mygallery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class SQLConnector
{
    private static Connection connection;
    private static Statement statement;

    public static void initialize()
    {
        try
        {
            //connecting to sqlite db
            connection = DriverManager.getConnection("jdbc:sqlite:myDB");
            statement = connection.createStatement();

            ResultSet check = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'files';");

            //creates table if one doesn't exist
            if (!check.next())
            {
                statement.execute("CREATE TABLE IF NOT EXISTS files (" +
                        "id integer PRIMARY KEY," +
                        "name varchar(32)," +
                        "path varchar(128));");
            }
        }
        catch(SQLException e) { e.printStackTrace(); }
    }

    public static void insert(ObservableList<ViewItem> viewItems) //inserts a list of items into the db
    {
        try
        {
            PreparedStatement insertPS = connection.prepareStatement("INSERT INTO files(name, path) VALUES(?, ?);");

            for (ViewItem vi : viewItems)
            {
                if (!containsFile(vi))
                {
                    insertPS.setString(1, vi.getName());
                    insertPS.setString(2, vi.getPath());
                    insertPS.execute();
                }
            }
        }
        catch(SQLException e) { e.printStackTrace(); }
    }

    public static void remove(ObservableList<ViewItem> viewItems) //removes a list of items from the db
    {
        try
        {
            PreparedStatement deletePS = connection.prepareStatement("DELETE FROM files WHERE name = ? AND path = ?;");

            for(ViewItem vi : viewItems)
            {
                deletePS.setString(1, vi.getName());
                deletePS.setString(2, vi.getPath());
                deletePS.execute();
            }
        }
        catch(SQLException e) { e.printStackTrace(); }
    }

    public static ObservableList<ViewItem> getDBItems() //returns a list of all the items in the db
    {
        try
        {
            ObservableList<ViewItem> viewItems = FXCollections.observableArrayList();

            ResultSet rs = statement.executeQuery("SELECT * FROM files");

            while (rs.next())
                viewItems.add(new ViewItem(rs.getString("name"), rs.getString("path")));

            return viewItems;
        }
        catch(SQLException e) { e.printStackTrace(); return null;}
    }

    private static boolean containsFile(ViewItem vi) throws SQLException //checks if an item exists in the db
    {
        ResultSet rs = statement.executeQuery("SELECT * FROM files");

        while(rs.next())
            if(rs.getString("name").equals(vi.getName()) && rs.getString("path").equals(vi.getPath())) return true;

        return false;
    }
}
