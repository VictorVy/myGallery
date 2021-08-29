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

    //inserts files into db
    public static void insertFiles(ObservableList<ViewItem> viewItems)
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
    //inserts tags into db
    public static void insertTags(ObservableList<String> tags)
    {
        try
        {
            for(String t : tags)
                if(!containsTag(t))
                    statement.execute("INSERT INTO tags(name) VALUES('" + t + "')");
        }
        catch (SQLException e) { e.printStackTrace(); }

//        try { statement.execute("DELETE FROM tags"); } catch(SQLException e) { e.printStackTrace(); }
//        print();
    }
    //inserts entry into cross-reference table
    public static void insertXRef(String[] itemPaths, String[] tagNames)
    {
        try
        {
            for(int i = 0; i < itemPaths.length; i++)
                statement.execute("INSERT INTO fileTagXRef VALUES('" + getFileId(itemPaths[i]) + "', '" + getTagId(tagNames[i]) + "')");
        }
        catch (SQLException e) { e.printStackTrace(); }

//        try { statement.execute("DELETE FROM fileTagXRef"); } catch(SQLException e) { e.printStackTrace(); }
//        print();
    }

    public static void removeFiles(ObservableList<ViewItem> viewItems)
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
    public static void removeTags(ObservableList<String> tags)
    {
        try
        {
            PreparedStatement deletePS = connection.prepareStatement("DELETE FROM tags WHERE name = ?;");

            for (String t : tags)
            {
                deletePS.setString(1, t);
                deletePS.execute();
            }
        }
        catch(SQLException e) { e.printStackTrace(); }
    }

    //getters related to the 'files' table
    public static ObservableList<ViewItem> getFiles()
    {
        try
        {
            ObservableList<ViewItem> viewItems = FXCollections.observableArrayList();

            if(statement != null) //I think necessary due to early calls in initializers
            {
                ResultSet rs = statement.executeQuery("SELECT * FROM files ORDER BY " + Main.mainController.sortBy + " " + (Main.mainController.ascending ? "ASC" : "DESC"));

                while (rs.next())
                    viewItems.add(new ViewItem(rs.getInt("fileID"),
                                               rs.getString("name"),
                                               rs.getString("type"),
                                               rs.getString("path"),
                                               rs.getString("cDate").substring(0, rs.getString("cDate").indexOf('T')),
                                               rs.getString("aDate").substring(0, rs.getString("cDate").indexOf('T'))));
            }

            return viewItems;
        }
        catch (SQLException e) { e.printStackTrace(); return null; }
    }
    public static int getFileId(String path)
    {
        try
        {
            ResultSet rs = statement.executeQuery("SELECT fileID FROM files WHERE path LIKE '" + path + "'");
            if(rs.next())
                return rs.getInt("fileID");
            return -1;
        }
        catch(SQLException e) { e.printStackTrace(); return -1; }
    }
    //getters related to the 'tags' table
    public static ObservableList<String> getTags()
    {
        try
        {
            ObservableList<String> tags = FXCollections.observableArrayList();

            if(statement != null)
            {
                ResultSet rs = statement.executeQuery("SELECT name FROM tags");

                while(rs.next())
                    tags.add(rs.getString("name"));
            }

            return tags;
        }
        catch(SQLException e) { e.printStackTrace(); return null; }
    }
    public static int getTagId(String name)
    {
        try
        {
            ResultSet rs = statement.executeQuery("SELECT tagID FROM tags WHERE name LIKE '" + name + "'");
            if(rs.next())
                return rs.getInt("tagID");
            return -1;
        }
        catch(SQLException e) { e.printStackTrace(); return -1; }
    }

    // ---- UTILITY METHODS ---- //

    //checks if a file exists in the db
    private static boolean containsFile(ViewItem vi) throws SQLException
    {
        ResultSet rs = statement.executeQuery("SELECT path FROM files");

        while(rs.next())
            if(rs.getString("path").equals(vi.getPath())) return true;

        return false;
    }
    //checks if a tag exists in the db
    private static boolean containsTag(String t) throws SQLException
    {
        ResultSet rs = statement.executeQuery("SELECT name FROM tags");

        while(rs.next())
            if(rs.getString("name").equals(t)) return true;

        return false;
    }

    public static void close()
    {
        try { connection.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    public static void print()
    {
//        try
//        {
//            ResultSet rs = statement.executeQuery("SELECT f.name AS fileName, t.name AS tagName " +
//                                                      "FROM files f, tags t, fileTagXRef x " +
//                                                      "WHERE f.fileID = x.fileID AND x.tagID = t.tagID");
//            while(rs.next())
//                System.out.println(rs.getString("fileName") + " : " + rs.getString("tagName"));
//        }
//        catch(Exception e) { }

//        try
//        {
//            ResultSet rs = statement.executeQuery("SELECT * FROM tags");
//            while(rs.next())
//                System.out.println(rs.getString("name"));
//            rs = statement.executeQuery("SELECT * FROM fileTagXRef");
//            while(rs.next())
//                System.out.println(rs.getInt("fileID") + " : " + rs.getInt("tagID"));
//        }
//        catch(SQLException e)
//        {
//            e.printStackTrace();
//        }
    }
}