package controller;

import authentication.AuthenticationException;
import authentication.AuthenticationService;
import authentication.HttpBasicAuthService;
import com.mongodb.Mongo;
import storage.*;

import java.util.Set;


/**
 * Created by Matthew on 9/7/2015.
 */
public class ServiceController {
    //private static final AuthenticationService DEFAULT_AUTH_SERVICE = HttpBasicAuthService.getInstance();
    //private static final StorageService DEFAULT_STORAGE_SERVICE = MongoDBStorageService.getInstance();
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
        if (storageService == null) {
            storageService = MongoDBStorageService.getInstance();
        }
        return storageService;
    }

    public AuthenticationService getAuthService() {
        if (authService == null) {
            authService = HttpBasicAuthService.getInstance();
        }
        return authService;
    }

    public void createUser(String username, String password, String firstName, String lastName) throws HTTPException {
        try {
            getStorageService().createUser(username, password, firstName, lastName);
        } catch (UserAlreadyExistsException e) {
            throw new HTTPException("Conflict: Username is already in use", 409);
        } catch (StorageException e) {
            throw new HTTPException("Error creating user", 500);
        }
    }

    public void authenticate(String authtoken) throws HTTPException {
        getAuthService().authenticate(authtoken);
    }

    public void validate(String authtoken, String target) throws HTTPException {
        getAuthService().validate(authtoken, target);
    }

    public void sendFriendRequest(String senderUsername, String targetUsername) throws HTTPException {
        getStorageService().insertFriendRequest(senderUsername, targetUsername);
    }

    public Set getFriendRequestsFor(String user) throws HTTPException {
        return getStorageService().getFriendRequestsFor(user);
    }

    public void acceptFriendRequest(String acceptor, String acceptee) throws HTTPException {
        getStorageService().createFriendship(acceptor, acceptee);
    }

    public void removeFriend(String user, String target) throws HTTPException {
        getStorageService().removeFriend(user, target);
    }

    public Set getFriends(String user) throws HTTPException {
        return getStorageService().getFriendsFor(user);
    }
}
