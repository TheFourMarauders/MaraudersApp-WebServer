package controller;

import authentication.AuthenticationService;
import authentication.HttpBasicAuthService;
import storage.MemoryStorageService;
import storage.MongoDBStorageService;
import storage.StorageService;

/**
 * Created by Joe on 9/16/2015.
 */
public class ServiceFactory {

    public ServiceController buildLocal() {
        AuthConfig authConfig = new AuthConfig();
        StorageService storageService = new MemoryStorageService(authConfig);
        AuthenticationService authService = new HttpBasicAuthService(authConfig, storageService);

        ServiceController sc = new ServiceController(authService, storageService);

        return sc;
    }

    public ServiceController build() {
        return build(new AuthConfig(), new DatabaseConfig());
    }

    public ServiceController build(DatabaseConfig dbConfig) {
        return build(new AuthConfig(), dbConfig);
    }

    public ServiceController build(AuthConfig authConfig, DatabaseConfig dbConfig) {
        StorageService storageService = new MongoDBStorageService(dbConfig, authConfig);
        AuthenticationService authService = new HttpBasicAuthService(authConfig, storageService);

        ServiceController sc = new ServiceController(authService, storageService);

        return sc;
    }
}
