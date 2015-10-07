package controller;

import TheFourMarauders.ServerConfig;
import authentication.AuthenticationService;
import authentication.HttpBasicAuthService;
import storage.MemoryStorageService;
import storage.MongoDBStorageService;
import storage.StorageService;

/**
 * Created by Joe on 9/16/2015.
 */
public class ServiceFactory {

    public ServiceController build() {
        return build(new AuthConfig(), new StorageConfig());
    }

    public ServiceController build(StorageConfig dbConfig) {
        return build(new AuthConfig(), dbConfig);
    }

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

    public ServiceController build(ServerConfig conf) {
        if (conf == null) {
            return build();
        }
        return build(conf.getAuthConfig(), conf.getStorageConfig());
    }
}
