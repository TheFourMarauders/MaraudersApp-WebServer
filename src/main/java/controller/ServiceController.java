package controller;

import authentication.AuthenticationService;
import storage.*;
import storage.datatypes.GroupInfo;
import storage.datatypes.LocationInfo;
import storage.datatypes.UserInfo;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Interface between WebServer endpoints and back end services (auth and storage)
 *
 * @author Matt and Joe
 * @version 1.0
 */
public class ServiceController {

    private AuthenticationService authService;
    private StorageService storageService;

    public ServiceController(AuthenticationService authService, StorageService storageService) {
        this.authService = authService;
        this.storageService = storageService;
    }

    /**
     *
     * @param username
     * @param password
     * @param firstName
     * @param lastName
     * @throws HTTPException if username is empty or in use, or if storage fails
     */
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

    /**
     * Verifies the authentication of a user given an authtoken
     *
     * @param authtoken
     * @throws HTTPException if authtoken is invalid
     */
    public void authenticate(String authtoken) throws HTTPException {
        authService.authenticate(authtoken);
    }

    /**
     *
     * @param authtoken
     * @param senderUsername
     * @param targetUsername
     * @throws HTTPException if user tries to friend himself,
     *         or if target doesn't exist, or if auth fails
     */
    public void sendFriendRequest(String authtoken, String senderUsername, String targetUsername) throws HTTPException {
        authService.validate(authtoken, senderUsername);
        if (senderUsername.equals(targetUsername)) {
            throw new HTTPException("You are already your own best friend :)", 200);
        }
        storageService.insertFriendRequest(senderUsername, targetUsername);
    }

    /**
     *
     * @param authtoken
     * @param user
     * @return the user's friends
     * @throws HTTPException if auth fails
     */
    public Set<UserInfo> getFriendRequestsFor(String authtoken, String user) throws HTTPException {
        authService.validate(authtoken, user);
        return storageService.getFriendRequestsFor(user);
    }

    /**
     *
     * @param authtoken
     * @param acceptor  the user accepting the friend request
     * @param acceptee  the user whose friend request is being accepted
     * @throws HTTPException if auth fails or if there is no friend request from acceptee
     */
    public void acceptFriendRequest(String authtoken, String acceptor, String acceptee) throws HTTPException {
        authService.validate(authtoken, acceptor);
        storageService.createFriendship(acceptor, acceptee);
    }

    /**
     *
     * @param authtoken
     * @param user  the user deleting a friend
     * @param target    the friend being deleted
     * @throws HTTPException if auth fails, or if the user's aren't friends
     */
    public void deleteFriend(String authtoken, String user, String target) throws HTTPException {
        authService.validate(authtoken, user);
        storageService.removeFriend(user, target);
    }

    /**
     *
     * @param authtoken
     * @param user
     * @return  the set of this user's friends
     * @throws HTTPException    if auth fails
     */
    public Set<UserInfo> getFriends(String authtoken, String user) throws HTTPException {
        authService.validate(authtoken, user);
        return storageService.getFriendsFor(user);
    }

    /**
     *
     * @param authtoken
     * @param user  the user whose locations we are getting
     * @param start the time of the earliest location we want
     * @param end   the time of the last location we want
     * @return  the set of all locations for this user within the time range defined by start and end
     * @throws HTTPException    if auth fails or if user trying to access a non-friend
     */
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

    /**
     *
     * @param authtoken
     * @param user
     * @param list  a list of LocationInfo to add to storage
     * @throws HTTPException     if auth or storage fails
     */
    public void putLocationsFor(String authtoken, String user, List<LocationInfo> list) throws HTTPException {
        authService.validate(authtoken, user);
        storageService.addLocationsToUser(user, list);

    }

    /**
     * Makes a new group with the given name
     * @param authtoken
     * @param groupName the desired name for this group (note does not have to be unique among all groups)
     * @return  a unique id for this new group
     * @throws HTTPException    if auth or storage fails
     */
    public String createGroup(String authtoken, String groupName) throws HTTPException {
        return storageService.createGroup(authService.getUsernameFromAuthToken(authtoken), groupName);
    }

    /**
     *
     * @param authtoken
     * @param username
     * @return  the groups that this user is a member of
     * @throws HTTPException    if auth or storage access fail
     */
    public Set<GroupInfo> getGroups(String authtoken, String username) throws HTTPException {
        authService.validate(authtoken, username);
        return storageService.getGroupsForUser(username);
    }

    /**
     *
     * @param authtoken
     * @param id    the id of the group whose info we want
     * @return  returns a GroupInfo, which has the name and usernames of members
     * @throws HTTPException    if user is not in the requested group or if auth fails
     */
    public GroupInfo getGroup(String authtoken, String id) throws HTTPException {
        authService.validateGroupAccess(authtoken, id);
        return storageService.getGroupById(id);
    }

    /**
     *
     * @param authtoken
     * @param userToAdd
     * @param groupId
     * @throws HTTPException    if you are not friends with userToAdd
     *                          or if you are not in the group addressed by groupId
     *                          or if auth fails
     */
    public void putUserInGroup(String authtoken, String userToAdd, String groupId) throws HTTPException {
        authService.validateFriendAccess(authtoken, userToAdd);
        authService.validateGroupAccess(authtoken, groupId);
        storageService.addUserToGroup(userToAdd, groupId);
    }

    /**
     *
     * @param authtoken
     * @param userToRemove
     * @param groupId
     * @throws HTTPException    if you're not in the group defined by groupId or if auth fails
     */
    public void deleteUserFromGroup(String authtoken, String userToRemove, String groupId) throws HTTPException {
        authService.validateGroupAccess(authtoken, groupId);
        storageService.removeUserFromGroup(userToRemove, groupId);
    }

    /**
     *
     * @param authtoken
     * @param groupId
     * @param start the time of the earliest desired location
     * @param end   the time of the last desired location
     * @return  a map from username to their locations within the time range start to end
     * @throws HTTPException    if you are not in the group or if auth fails
     */
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