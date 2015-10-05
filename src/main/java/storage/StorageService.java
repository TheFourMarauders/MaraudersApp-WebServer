package storage;

import controller.HTTPException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * Created by Matthew on 9/7/2015.
 */
public interface StorageService {
    byte[] getHashedPassword(String username) throws HTTPException;

    void createUser(String username, String password, String firstName, String lastName)
            throws HTTPException;

    void insertFriendRequest(String senderUsername, String targetUsername) throws HTTPException;

    boolean areUsersFriends(String username1, String username2) throws HTTPException;

    Set<UserInfo> getFriendRequestsFor(String username) throws HTTPException;

    Set<UserInfo> getFriendsFor(String username) throws HTTPException;

    void createFriendship(String frAcceptor, String frSender) throws HTTPException;

    void removeFriend(String removerUsername, String removeeUsername) throws HTTPException;

    void addLocationsToUser(String username, List<LocationInfo> locations) throws HTTPException;

    List<LocationInfo> getLocationsForUser(String username, ZonedDateTime before, ZonedDateTime after) throws HTTPException;
}
