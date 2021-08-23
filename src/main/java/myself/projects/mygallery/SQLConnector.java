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
            connection = DriverManager.getConnection("jdbc:sqlite::resource:db/myDB.db");
            statement = connection.createStatement();

            initializeTables();
        }
        catch (SQLException e) { e.printStackTrace(); }
    }
    //creates tables if they don't already exist
    private static void initializeTables() throws SQLException
    {
        //checking files table
        ResultSet check = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'files';");
        if (!check.next())
            statement.execute("CREATE TABLE files (fileID INTEGER PRIMARY KEY," +
                                                      "name VARCHAR(256)," +
                                                      "type VARCHAR(64)," +
                                                      "path VARCHAR(256)," +
                                                      "cDate VARCHAR(64)," +
                                                      "aDate VARCHAR(64));");
        //checking tags table
        check = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'tags';");
        if (!check.next())
            statement.execute("CREATE TABLE tags (tagID INTEGER PRIMARY KEY," +
                                                     "name VARCHAR(128));");

        //checking associative entity (cross-reference) table
        check = statement.executeQuery("SELECT * FROM sqlite_master WHERE type = 'table' AND name = 'fileTagXRef';");
        if (!check.next())
            statement.execute("CREATE TABLE fileTagXRef (fileID INTEGER," +
                                                            "tagID INTEGER," +
                                                            "PRIMARY KEY (fileID, tagID));");
    }

    //inserts a list of items into the db
    public static void insert(ObservableList<ViewItem> viewItems)
    {
        try
        {
            PreparedStatement insertPS = connection.prepareStatement("INSERT INTO files(name, type, path, cDate, aDate) VALUES(?, ?, ?, ?, ?);");

            for (ViewItem vi : viewItems)
            {
                if (!containsFile(vi))
                {
                    insertPS.setString(1, vi.getName());
                    insertPS.setString(2, vi.getType());
                    insertPS.setString(3, vi.getPath());
                    insertPS.setString(4, vi.getCDate());
                    insertPS.setString(5, vi.getADate());
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
                ResultSet rs = statement.executeQuery("SELECT * FROM files ORDER BY " + Main.mainController.sortBy + " " + (Main.mainController.ascending ? "ASC" : "DESC"));

                while (rs.next())
                    viewItems.add(new ViewItem(rs.getString("name"),
                                               rs.getString("type"),
                                               rs.getString("path"),
                                               rs.getString("cDate").substring(0, rs.getString("cDate").indexOf('T')),
                                               rs.getString("aDate").substring(0, rs.getString("cDate").indexOf('T'))));
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
        try { connection.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }

//    public static void test()
//    {
//        try
//        {
//            ResultSet rs = statement.executeQuery("SELECT id FROM files");
//
//            while(rs.next())
//                System.out.println(rs.getInt("id"));
//        }
//        catch(Exception e) { return; }
//    }
}
