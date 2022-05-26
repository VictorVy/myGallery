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

//        checking preferences table
        check = statement.executeQuery("SELECT * FROM sqlite_master WHERE type = 'table' AND name = 'prefs';");
        if (!check.next())
        {
            statement.execute("CREATE TABLE prefs (ID INTEGER PRIMARY KEY," +
                                                  "font VARCHAR(64)," +
                                                  "size INTEGER," +
                                                  "theme VARCHAR(64));");
            //setting default prefs
            statement.execute("INSERT INTO prefs (font, size, theme) VALUES('System', 12, 'Light');");
        }
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
                    //kind of bulky... replace with statement.execute(...)?
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
            {
                if(!t.isEmpty() && !containsTag(t))
                    statement.execute("INSERT INTO tags(name) VALUES('" + t + "');");
            }
        }
        catch (SQLException e) { e.printStackTrace(); }
    }
    //inserts entry into cross-reference table
    public static void insertXRef(int fileID, ObservableList<String> tagNames)
    {
        try
        {
            for(String tn : tagNames)
            {
                if(!containsXRef(fileID, tn)) //diff from others... needs validation... why?
                    statement.execute("INSERT INTO fileTagXRef VALUES('" + fileID + "', '" + getTagId(tn) + "');");
            }
        }
        catch (SQLException e) { e.printStackTrace(); }
    }

    public static void removeFiles(ObservableList<ViewItem> viewItems)
    {
        try
        {
            for (ViewItem vi : viewItems)
            {
                statement.execute("DELETE FROM fileTagXRef WHERE fileID LIKE '" + vi.getId() + "';"); //remove xref entries
                statement.execute("DELETE FROM files WHERE path = '" + vi.getPath() + "';"); //then remove file
            }
        }
        catch (SQLException e) { e.printStackTrace(); }
    }
    public static void removeTags(ObservableList<String> tags)
    {
        try
        {
            for (String t : tags)
            {
                //took 2 hours to figure out that SQLite doesn't support delete joins... TODO: learn sql transactions
//                statement.execute("DELETE FROM tags t " +
//                                  "LEFT JOIN fileTagXRef AS x " +
//                                  "ON t.tagID = x.tagID " +
//                                  "WHERE t.name = '" + t + "';");

                statement.execute("DELETE FROM fileTagXRef WHERE tagID LIKE '" + getTagId(t) + "';"); //remove xref entries
                statement.execute("DELETE FROM tags WHERE name LIKE '" + t + "';"); //then remove tag
            }
        }
        catch(SQLException e) { e.printStackTrace(); }
    }
    public static void removeXRef(int fileID, ObservableList<String> tagName)
    {
        try
        {
            for(String tn : tagName)
                 statement.execute("DELETE FROM fileTagXRef WHERE fileID LIKE '" + fileID + "' AND tagID LIKE '" + getTagId(tn)+ "';");
        }
        catch(SQLException e) { e.printStackTrace(); }
    }

    public static void renameTag(String oldName, String newName)
    {
        try
        {
            statement.execute("UPDATE tags SET name = '" + newName + "' WHERE name LIKE '" + oldName + "';");
        }
        catch(SQLException e) { e.printStackTrace(); }
    }

    //getters related to the 'files' table
    public static ObservableList<ViewItem> getFiles()
    {
        try
        {
            ObservableList<ViewItem> viewItems = FXCollections.observableArrayList();

            if(statement != null) //think this is necessary due to early calls in initializers :^(
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
    public static ObservableList<ViewItem> searchFiles(String raw)
    {
        if(raw == null || raw.isEmpty()) return getFiles();

        try
        {
            ObservableList<ViewItem> viewItems = FXCollections.observableArrayList();
            String[] tokens = raw.trim().split("\\s+");
            boolean single = tokens.length == 1;

            if(statement != null)
            {
                boolean name = Main.mainController.searchName.isSelected(), type = Main.mainController.searchType.isSelected(), tags = Main.mainController.searchTags.isSelected();
                //TODO: replace convoluted string manipulation with sqlite fts5 functionality
                StringBuilder query = new StringBuilder("SELECT files.fileID, files.name, files.type, files.path, files.aDate, files.cDate FROM files ");
                StringBuilder builder = new StringBuilder();

                for(int i = 0; i < tokens.length; i++)
                    builder.append(i == 0 ? "" : "AND ").append("files.name LIKE '%").append(tokens[i]).append("%' ");

                if(name)
                {
                    query.append("WHERE (").append(builder).append(") ");

                    if(type || tags)
                    {
                        query = new StringBuilder(query.toString().replaceAll("AND", "OR"));
                        if(type) query.append(single ? "OR (" : "AND (").append(builder.toString().replaceAll("name", "type").replaceAll("AND", "OR")).append(") ");
                    }
                }
                else if(type) query.append("WHERE (").append(builder.toString().replaceAll("name", "type")).append(") ");

                if(tags)
                {

                    StringBuilder tagsQuery = new StringBuilder("SELECT DISTINCT files.fileID, files.name, files.type, files.path, files.aDate, files.cDate FROM files, tags, fileTagXRef " +
                                                                "WHERE files.fileID = fileTagXRef.fileID AND fileTagXRef.tagID = tags.tagID AND (");

                    for(int i = 0; i < tokens.length; i++)
                        tagsQuery.append(i == 0 ? "" : (name || type ? "OR " : "AND ")).append("files.fileID IN (SELECT fileTagXRef.fileID from fileTagXRef, tags WHERE fileTagXRef.tagID = tags.tagID AND tags.name LIKE '%").append(tokens[i]).append("%') ");
                    tagsQuery.append(") ");

                    if(name || type) query.append("INTERSECT ").append(tagsQuery);
                    else query = tagsQuery;
                }
                String check = query.append("ORDER BY files.").append(Main.mainController.sortBy).append(" ").append(Main.mainController.ascending ? "ASC" : "DESC").toString();
//                System.out.println(check);
                ResultSet rs = statement.executeQuery(check);

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
        catch(SQLException e) { e.printStackTrace(); return null; }
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
    public static ObservableList<String> getItemTags(ViewItem vi, boolean exclude)
    {
        try
        {
            ObservableList<String> tags = FXCollections.observableArrayList();

            if(statement != null)
            {
                ResultSet rs = statement.executeQuery("SELECT name AS tagName FROM tags WHERE tagName " + (exclude ? "NOT " : "") + "IN (" +
                                                      "SELECT t.name FROM tags t JOIN fileTagXRef x ON t.tagID = x.tagID " +
                                                      "WHERE x.fileID LIKE '" + vi.getId() + "');");

                while(rs.next())
                    tags.add(rs.getString("tagName"));
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
            if(rs.next()) return rs.getInt("tagID");
            return -1;
        }
        catch(SQLException e) { e.printStackTrace(); return -1; }
    }

    // ---- UTILITY METHODS ---- //

    //checks if a file exists
    private static boolean containsFile(ViewItem vi) throws SQLException { return statement.executeQuery("SELECT path FROM files WHERE path LIKE '" + vi.getPath() + "';").next(); }
    //checks if a tag exists in the db
    private static boolean containsTag(String t) throws SQLException { return statement.executeQuery("SELECT name FROM tags WHERE name LIKE '" + t + "';").next(); }
    //checks if an xref entry exists
    private static boolean containsXRef(int fileID, String t) throws SQLException { return statement.executeQuery("SELECT * FROM fileTagXRef WHERE fileID LIKE '" + fileID + "' AND tagID LIKE '" + getTagId(t) + "';").next(); }

    public static void updatePrefs(String font, Integer size, String theme)
    {
        try { statement.execute("UPDATE prefs SET font = '" + font + "', size = " + size + ", theme = '" + theme + "';"); }
        catch(SQLException e) { e.printStackTrace(); }
    }

    public static Object[] getPrefs()
    {
        try
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM prefs;");
            return new Object[] { rs.getString("font"), rs.getInt("size"), rs.getString("theme") };
        }
        catch(SQLException e) { e.printStackTrace(); return new Object[] { "System", 12, "Light" }; }
    }

    public static void close()
    {
        try { connection.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}