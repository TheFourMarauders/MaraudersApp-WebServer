package controller;

import authentication.AuthenticationService;
import storage.*;
import storage.datatypes.GroupInfo;
import storage.datatypes.LocationInfo;

import java.time.ZonedDateTime;
import java.util.*;


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

    public void sendFriendRequest(String authtoken, String senderUsername, String targetUsername) throws HTTPException {
        authService.validate(authtoken, senderUsername);
        if (senderUsername.equals(targetUsername)) {
            throw new HTTPException("You are already your own best friend :)", 200);
        }
        storageService.insertFriendRequest(senderUsername, targetUsername);
    }

    public Set getFriendRequestsFor(String authtoken, String user) throws HTTPException {
        authService.validate(authtoken, user);
        return storageService.getFriendRequestsFor(user);
    }

    public void acceptFriendRequest(String authtoken, String acceptor, String acceptee) throws HTTPException {
        authService.validate(authtoken, acceptor);
        storageService.createFriendship(acceptor, acceptee);
    }

    public void deleteFriend(String authtoken, String user, String target) throws HTTPException {
        authService.validate(authtoken, user);
        storageService.removeFriend(user, target);
    }

    public Set getFriends(String authtoken, String user) throws HTTPException {
        authService.validate(authtoken, user);
        return storageService.getFriendsFor(user);
    }

    public List<LocationInfo> getLocationsFor(String authtoken, String user, ZonedDateTime start, ZonedDateTime end) throws HTTPException {
        authService.validateFriendAccess(authtoken, user);
        if (start == null) {
            start = ZonedDateTime.now();
            start = start.minusYears(start.getYear());
        }
        if (end == null) {
            end = ZonedDateTime.now().plusDays(1);
        }
        return storageService.getLocationsForUser(user, start, end);
    }

    public void putLocationsFor(String authtoken, String user, List<LocationInfo> list) throws HTTPException {
        authService.validate(authtoken, user);
        storageService.addLocationsToUser(user, list);

    }

    public String createGroup(String authtoken, String groupName) throws HTTPException {
        return storageService.createGroup(authService.getUsernameFromAuthToken(authtoken), groupName);
    }

    public Set<GroupInfo> getGroups(String authtoken, String username) throws HTTPException {
        authService.validate(authtoken, username);
        return storageService.getGroupsForUser(username);
    }

    public GroupInfo getGroup(String authtoken, String id) throws HTTPException {
        authService.validateGroupAccess(authtoken, id);
        return storageService.getGroupById(id);
    }

    public void putUserInGroup(String authtoken, String userToAdd, String groupId) throws HTTPException {
        authService.validateFriendAccess(authtoken, userToAdd);
        authService.validateGroupAccess(authtoken, groupId);
        storageService.addUserToGroup(userToAdd, groupId);
    }

    public void deleteUserFromGroup(String authtoken, String userToRemove, String groupId) throws HTTPException {
        authService.validateGroupAccess(authtoken, groupId);
        storageService.removeUserFromGroup(userToRemove, groupId);
    }

    public Map<String, List<LocationInfo>> getLocationsForGroup(String authtoken, String groupId, ZonedDateTime start,
                                                                ZonedDateTime end) throws HTTPException {
        authService.validateGroupAccess(authtoken, groupId);
        Map<String, List<LocationInfo>> locationMap = new HashMap<>();
        GroupInfo group = storageService.getGroupById(groupId);
        for (String uid : group.getMembers()) {
            List<LocationInfo> locations = null;
            try {
                locations = getLocationsFor(authtoken, uid, start, end);
            } catch (HTTPException e) {
                locations = new ArrayList<>();
            }
            locationMap.put(uid, locations);
        }
        return locationMap;
    }
}