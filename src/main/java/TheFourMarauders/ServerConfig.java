package TheFourMarauders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import controller.AuthConfig;
import controller.StorageConfig;

/**
 * Created by joe on 10/7/15.
 */
public class ServerConfig {
    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_THREADS = 1;

    private int port;
    private int maxThreads;

    private StorageConfig storageConfig;
    private AuthConfig authConfig;

    public ServerConfig() {
        this(new StorageConfig(), new AuthConfig(), DEFAULT_PORT, DEFAULT_THREADS);
    }

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

    public int getPort() {
        return port;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public StorageConfig getStorageConfig() {
        return storageConfig;
    }

    public AuthConfig getAuthConfig() {
        return authConfig;
    }

    private void verifyTypes() {
        storageConfig.setStorageType(StorageConfig.StorageType.verify(storageConfig.getType()));
        authConfig.setAuthType(AuthConfig.AuthType.verify(authConfig.getType()));
    }

    public void setPort(int port) {
        this.port = port;
    }
}
