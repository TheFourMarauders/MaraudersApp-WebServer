package controller;


/**
 * Created by Matthew on 9/7/2015.
 */
public class DatabaseConfig {
    public static final String DEFAULT_URL = "localhost";
    public static final String DEFAULT_DBNAME = "MaraudersApp";

    private String url;
    private String dbName;

    public DatabaseConfig(){
        this(DEFAULT_URL, DEFAULT_DBNAME);
    }

    public DatabaseConfig(String url, String dbName) {
        this.url = url;
        this.dbName = dbName;
    }

    public String getURL(){
        return url;
    }
    public String getDbName(){
        return dbName;
    }
}
