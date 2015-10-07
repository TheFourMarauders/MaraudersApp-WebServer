package controller;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Matthew on 9/7/2015.
 */
public class StorageConfig {
    public static final List<String> DEFAULT_URLS = Arrays.asList(new String[]{"localhost"});
    public static final String DEFAULT_DBNAME = "MaraudersApp";
    public static final String DEFAULT_TYPE = "mem";

    private List<String> urls;
    private String dbName;
    private String type;

    private StorageType storageType;

    public StorageConfig(){
        this(DEFAULT_TYPE, DEFAULT_DBNAME, DEFAULT_URLS);
    }

    @JsonCreator
    public StorageConfig(
            @JsonProperty("type") String type,
            @JsonProperty("dbname") String dbName,
            @JsonProperty("urls") List<String> urls) {
        this.type = type;
        this.urls = new ArrayList<>();
        this.urls.addAll(urls);
        this.dbName = dbName;
    }

    public String getType() {
        return type;
    }

    public List<String> getURLS(){
        return urls;
    }
    public String getDbName(){
        return dbName;
    }

    public void setStorageType(StorageType s) {
        storageType = s;
    }

    public enum StorageType {
        MEMORY, MONGO;

        public static StorageType verify(String s) {
            if (s.equalsIgnoreCase("mem")) {
                return MEMORY;
            } else if (s.equalsIgnoreCase("mongo")) {
                return MONGO;
            } else {
                throw new IllegalArgumentException("Storage type " + s + " does not match any known type");
            }
        }
    }
}
