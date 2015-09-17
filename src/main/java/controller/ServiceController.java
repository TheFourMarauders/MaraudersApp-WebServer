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

    private AuthenticationService authService;
    private StorageService storageService;

    public ServiceController(AuthenticationService authService, StorageService storageService) {
        this.authService = authService;
        this.storageService = storageService;
    }

    public void createUser(String username, String password, String firstName, String lastName) throws HTTPException {
        try {
            if (!isValid(username) || !isValid(password) || !isValid(firstName) || !isValid(lastName)) {
                throw new HTTPException("Invalid user information: empty field", 400);
            }
            storageService.createUser(username, password, firstName, lastName);
        } catch (UserAlreadyExistsException e) {
            throw new HTTPException("Conflict: Username is already in use", 409);
        } catch (StorageException e) {
            throw new HTTPException("Error creating user", 500);
        }
    }

    private boolean isValid(String info) {
        return info != null && info.length() > 0;
    }

    public void authenticate(String authtoken) throws HTTPException {
        authService.authenticate(authtoken);
    }

    public void validate(String authtoken, String target) throws HTTPException {
        authService.validate(authtoken, target);
    }

    public void sendFriendRequest(String senderUsername, String targetUsername) throws HTTPException {
        storageService.insertFriendRequest(senderUsername, targetUsername);
    }

    public Set getFriendRequestsFor(String user) throws HTTPException {
        return storageService.getFriendRequestsFor(user);
    }

    public void acceptFriendRequest(String acceptor, String acceptee) throws HTTPException {
        storageService.createFriendship(acceptor, acceptee);
    }

    public void removeFriend(String user, String target) throws HTTPException {
        storageService.removeFriend(user, target);
    }

    public Set getFriends(String user) throws HTTPException {
        return storageService.getFriendsFor(user);
    }
}
