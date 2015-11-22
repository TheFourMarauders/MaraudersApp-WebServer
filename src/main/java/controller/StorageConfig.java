package controller;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines a configuration for a storage service, including any urls,
 * storage name, and type
 *
 * @author Matt and Joe
 * @version 1.0
 */
public class StorageConfig {
    public static final List<String> DEFAULT_URLS = Arrays.asList(new String[]{"localhost"});
    public static final String DEFAULT_DBNAME = "MaraudersApp";
    public static final String DEFAULT_TYPE = "mem";

    private List<String> urls;
    private String dbName;
    private String type;

    private StorageType storageType;

    /**
     * Construct with default values
     */
    public StorageConfig(){
        this(DEFAULT_TYPE, DEFAULT_DBNAME, DEFAULT_URLS);
    }

    /**
     * Construct a StorageConfig with the given properties
     * @param type see StorageConfig.StorageType
     * @param dbName
     * @param urls
     */
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

    /**
     *
     * @return  a type, implemented include "mem" and "mongo"
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return  the urls at which a component of the storage service lives
     */
    public List<String> getURLS(){
        return urls;
    }

    /**
     *
     * @return
     */
    public String getDbName(){
        return dbName;
    }

    /**
     *
     * @param s
     */
    public void setStorageType(StorageType s) {
        storageType = s;
    }

    /**
     * Defines implemented StorageType, currently includes StorageType.MEMORY
     * and StorageType.MONGO
     *
     * @author Joe
     * @version 1.0
     */
    public enum StorageType {
        MEMORY, MONGO;

        /**
         *
         * @param s the name of a storage type, "mem" or "mongo"
         * @return  a StorageType that corresponds to a given name
         */
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
