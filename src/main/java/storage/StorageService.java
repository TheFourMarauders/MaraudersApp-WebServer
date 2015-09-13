package storage;

import controller.HTTPException;

import java.util.Set;

/**
 * Created by Matthew on 9/7/2015.
 */
public interface StorageService {
    byte[] getHashedPassword(String username) throws StorageException;

    void createUser(String username, String password, String firstName, String lastName)
            throws HTTPException;

    void insertFriendRequest(String senderUsername, String targetUser) throws HTTPException;

    boolean areUsersFriends(String username1, String username2) throws StorageException;

    Set<UserInfo> getFriendRequestsFor(String username) throws StorageException;

    Set<UserInfo> getFriendsFor(String username) throws StorageException;

    void createFriendship(String frAcceptor, String frSender) throws StorageException, FriendshipException;

    void removeFriend(String removerUsername, String removeeUsername) throws StorageException;
}
