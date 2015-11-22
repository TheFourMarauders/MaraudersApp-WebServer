package controller;

import TheFourMarauders.ServerConfig;
import authentication.AuthenticationService;
import authentication.HttpBasicAuthService;
import storage.MemoryStorageService;
import storage.MongoDBStorageService;
import storage.StorageService;

/**
 * Creates an instance of a ServiceController based on any combination of
 * AuthConfig and StorageConfig, or a ServiceConfig which contains both
 * AuthConfig and StorageConfig as well as other server params
 *
 * @author Joe
 * @version 1.0
 */
public class ServiceFactory {

    /**
     *
     * @return  ServiceController with default configs
     */
    public ServiceController build() {
        return build(new AuthConfig(), new StorageConfig());
    }

    /**
     *
     * @param dbConfig  a storage configuration
     * @return  ServiceController with default auth config and dbConfig
     */
    public ServiceController build(StorageConfig dbConfig) {
        return build(new AuthConfig(), dbConfig);
    }

    /**
     *
     * @param authConfig
     * @param dbConfig
     * @return  ServiceController with custom authConfgi and dbConfig
     */
    public ServiceController build(AuthConfig authConfig, StorageConfig dbConfig) {
        StorageService storageService = null;
        if (dbConfig.getType().equalsIgnoreCase("mem")) {
            storageService = new MemoryStorageService(authConfig);
        } else if (dbConfig.getType().equalsIgnoreCase("mongo")) {
            storageService = new MongoDBStorageService(dbConfig, authConfig);
        }
        AuthenticationService authService = new HttpBasicAuthService(authConfig, storageService);

        ServiceController sc = new ServiceController(authService, storageService);

        return sc;
    }


    /**
     *
     * @param conf
     * @return  ServiceController using the AuthConfig and StorageConfig
     *          as defined in ServerConfig conf
     */
    public ServiceController build(ServerConfig conf) {
        if (conf == null) {
            return build();
        }
        return build(conf.getAuthConfig(), conf.getStorageConfig());
    }
}
