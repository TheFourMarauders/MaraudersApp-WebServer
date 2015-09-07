package controller;

import authentication.AuthenticationService;
import authentication.HttpBasicAuthService;
import storage.MongoDBStorageService;
import storage.StorageService;

import javax.xml.ws.Service;

/**
 * Created by Matthew on 9/7/2015.
 */
public class ServiceController {
    private static final AuthenticationService DEFAULT_AUTH_SERVICE = HttpBasicAuthService.getInstance();
    private static final StorageService DEFAULT_STORAGE_SERVICE = MongoDBStorageService.getInstance();
    private static ServiceController instance;

    private AuthenticationService authService;
    private StorageService storageService;
    private DatabaseConfig dbConfig;
    private AuthConfig authConfig;


    public static ServiceController getInstance() {
        if(instance == null){
            instance = new ServiceController();
        }
        return instance;
    }
    private ServiceController() {
        this.authService = DEFAULT_AUTH_SERVICE;
        this.storageService = DEFAULT_STORAGE_SERVICE;
        this.dbConfig = new DatabaseConfig();
        this.authConfig = new AuthConfig();
    }

    public AuthConfig getAuthConfig() {
        return authConfig;
    }

    public DatabaseConfig getDbConfig() {
        return dbConfig;
    }

    public StorageService getStorageService() {
        return storageService;
    }

    public AuthenticationService getAuthService() {
        return authService;
    }

}
