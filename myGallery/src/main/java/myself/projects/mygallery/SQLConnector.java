package myself.projects.mygallery;

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
                statement.execute("CREATE TABLE files (" +
                        "id INTEGER PRIMARY KEY," +
                        "name VARCHAR(256)," +
                        "type VARCHAR(64)," +
                        "path VARCHAR(256)," +
                        "c_date VARCHAR(64));");
            }
        }
        catch (SQLException e) { e.printStackTrace(); }
    }

    //inserts a list of items into the db
    public static void insert(ObservableList<ViewItem> viewItems)
    {
        try
        {
            PreparedStatement insertPS = connection.prepareStatement("INSERT INTO files(name, type, path, c_date) VALUES(?, ?, ?, ?);");

            for (ViewItem vi : viewItems)
            {
                if (!containsFile(vi))
                {
                    insertPS.setString(1, vi.getName());
                    insertPS.setString(2, vi.getType());
                    insertPS.setString(3, vi.getPath());
                    insertPS.setString(4, vi.getCDate());
                    insertPS.execute();
                }
            }
        }
        catch (SQLException e) { e.printStackTrace(); }
    }

    //removes a list of items from the db
    public static void remove(ObservableList<ViewItem> viewItems)
    {
        try
        {
            PreparedStatement deletePS = connection.prepareStatement("DELETE FROM files WHERE path = ?;");

            for (ViewItem vi : viewItems)
            {
                deletePS.setString(1, vi.getPath());
                deletePS.execute();
            }
        }
        catch (SQLException e) { e.printStackTrace(); }
    }

    //returns a list of all the items in the db
    public static ObservableList<ViewItem> getDBItems()
    {
        try
        {
            ObservableList<ViewItem> viewItems = FXCollections.observableArrayList();

            if(statement != null)
            {
                ResultSet rs = statement.executeQuery("SELECT * FROM files");

                while (rs.next())
                    viewItems.add(new ViewItem(rs.getString("name"),
                            rs.getString("type"),
                            rs.getString("path"),
                            rs.getString("c_date").substring(0, rs.getString("c_date").indexOf('T'))));
            }

            return viewItems;
        }
        catch (SQLException e) { e.printStackTrace(); return null; }
    }

    //checks if an item exists in the db
    private static boolean containsFile(ViewItem vi) throws SQLException
    {
        ResultSet rs = statement.executeQuery("SELECT * FROM files");

        while (rs.next())
            if (rs.getString("path").equals(vi.getPath())) return true;

        return false;
    }

    public static void close()
    {
        try
        {
            connection.close();
        }
        catch (SQLException e) { e.printStackTrace(); }
    }
}
