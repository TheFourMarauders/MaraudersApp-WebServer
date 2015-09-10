package storage;

/**
 * Created by Matthew on 9/7/2015.
 */
public interface StorageService {
    byte[] getHashedPassword(String username) throws StorageException;

    void createUser(String username, String hashedPassword, String firstName, String lastName)
            throws StorageException;

    void insertFriendRequest(String senderUsername, String targetUser) throws StorageException;

    boolean areUsersFriends(String username1, String username2) throws StorageException;
}
