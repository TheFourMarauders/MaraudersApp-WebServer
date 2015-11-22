package storage;

import controller.HTTPException;
import storage.datatypes.GroupInfo;
import storage.datatypes.LocationInfo;
import storage.datatypes.UserInfo;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * Interface representing everything needed for a storage service
 * @author Matt and Joe
 */
public interface StorageService {
    /**
     * Retrieves the hashed password for a given username
     * @param username
     * @return a byte[] representing the hash
     * @throws HTTPException
     */
    byte[] getHashedPassword(String username) throws HTTPException;

    /**
     * creates a user with the given information
     * @param username
     * @param password
     * @param firstName
     * @param lastName
     * @throws HTTPException
     */
    void createUser(String username, String password, String firstName, String lastName)
            throws HTTPException;

    /**
     * adds a friend request to the target user from a source user
     * @param senderUsername
     * @param targetUsername
     * @throws HTTPException
     */
    void insertFriendRequest(String senderUsername, String targetUsername) throws HTTPException;

    /**
     * checks to see if 2 users are friends
     * @param username1
     * @param username2
     * @return
     * @throws HTTPException
     */
    boolean areUsersFriends(String username1, String username2) throws HTTPException;

    /**
     * Retrieves the set of friend requests for a given user
     * @param username
     * @return
     * @throws HTTPException
     */
    Set<UserInfo> getFriendRequestsFor(String username) throws HTTPException;

    /**
     * Retrieves a set of friends for a given user
     * @param username
     * @return
     * @throws HTTPException
     */
    Set<UserInfo> getFriendsFor(String username) throws HTTPException;

    /**
     * makes two users friends
     * @param frAcceptor
     * @param frSender
     * @throws HTTPException
     */
    void createFriendship(String frAcceptor, String frSender) throws HTTPException;

    /**
     * removes a friend from a given user
     * @param removerUsername
     * @param removeeUsername
     * @throws HTTPException
     */
    void removeFriend(String removerUsername, String removeeUsername) throws HTTPException;

    /**
     * adds the set of locations to the users history
     * @param username
     * @param locations
     * @throws HTTPException
     */
    void addLocationsToUser(String username, List<LocationInfo> locations) throws HTTPException;

    /**
     * Retrieves a list of sorted locations for the given user between the two times given
     * @param username
     * @param before
     * @param after
     * @return
     * @throws HTTPException
     */
    List<LocationInfo> getLocationsForUser(String username, ZonedDateTime before, ZonedDateTime after) throws HTTPException;


    /**
     * creates a gorup with the given group name with the given user as the first member
     * @param username
     * @param groupName
     * @return
     * @throws HTTPException
     */
    String createGroup(String username, String groupName) throws HTTPException;

    /**
     * Retrieves a set of groups for the given user
     * @param username
     * @return
     * @throws HTTPException
     */
    Set<GroupInfo> getGroupsForUser(String username) throws HTTPException;

    /**
     * retrieves the group info for a given ID
     * @param id
     * @return
     * @throws HTTPException
     */
    GroupInfo getGroupById(String id) throws HTTPException;

    /**
     * checks to see if the given user is in the given group
     * @param username
     * @param groupId
     * @return
     * @throws HTTPException
     */
    boolean isUserInGroup(String username, String groupId) throws HTTPException;

    /**
     * adds the given user to the given group
     * @param username
     * @param groupId
     * @throws HTTPException
     */
    void addUserToGroup(String username, String groupId) throws HTTPException;

    /**
     * removes the given user from the given group
     * @param username
     * @param groupId
     * @throws HTTPException
     */
    void removeUserFromGroup(String username, String groupId) throws HTTPException;
}
