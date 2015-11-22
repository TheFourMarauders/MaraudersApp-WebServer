package thefourmarauders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import controller.AuthConfig;
import controller.StorageConfig;

/**
 * Class representing the server configuration for running the server
 * @author Matt and Joe
 */
public class ServerConfig {
    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_THREADS = 1;

    private int port;
    private int maxThreads;

    private StorageConfig storageConfig;
    private AuthConfig authConfig;


    /**
     * No arg constructor with default values
     */
    public ServerConfig() {
        this(new StorageConfig(), new AuthConfig(), DEFAULT_PORT, DEFAULT_THREADS);
    }

    /**
     * Constructor in which you specify all the values, also used as the constructor for json deserialization
     * @param storageConfig - the StorageConfiguration
     * @param authConfig - the authentication configuration
     * @param port
     * @param maxThreads
     */
    @JsonCreator
    public ServerConfig(
            @JsonProperty("storage") StorageConfig storageConfig,
            @JsonProperty("auth") AuthConfig authConfig,
            @JsonProperty("port") int port,
            @JsonProperty("maxthreads") int maxThreads) {
        this.storageConfig = storageConfig;
        this.authConfig = authConfig;
        this.port = port;
        this.maxThreads = maxThreads;
        verifyTypes();
    }

    /**
     * gets the port
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * gets the max threads
     * @return the max  threads
     */
    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * gets the storage config
     * @return the config
     */
    public StorageConfig getStorageConfig() {
        return storageConfig;
    }

    /**
     * gets the auth config
     * @return the auth config
     */
    public AuthConfig getAuthConfig() {
        return authConfig;
    }

    private void verifyTypes() {
        storageConfig.setStorageType(StorageConfig.StorageType.verify(storageConfig.getType()));
        authConfig.setAuthType(AuthConfig.AuthType.verify(authConfig.getType()));
    }

    /**
     * sets the port
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }
}
