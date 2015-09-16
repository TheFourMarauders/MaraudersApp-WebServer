package storage;

import controller.AuthConfig;
import controller.HTTPException;
import controller.ServiceController;
import storage.mongostoragemodel.User;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Joe on 9/16/2015.
 */
public class MemoryStorageService implements StorageService {

    private Map<String, User> storage;

    public MemoryStorageService() {
        storage = new HashMap<>();
    }

    @Override
    public byte[] getHashedPassword(String username) throws StorageException {
        User u = storage.get(username);
        if (u == null) {
            throw new NoSuchUserException(username);
        }
        return Base64.getDecoder().decode(u.getHashedPassword());
    }

    @Override
    public void createUser(String username, String password, String firstName, String lastName) throws HTTPException {
        AuthConfig authConfig = ServiceController.getInstance().getAuthConfig();
        String encodedHashPass = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(authConfig.getHashAlgo());
            byte[] hashedPassword = digest.digest(password.getBytes(authConfig.getEncoding()));
            encodedHashPass = new String(Base64.getEncoder().encode(hashedPassword), "UTF8");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new HTTPException("Critical server error :(", 500);
        }
        User u = new User(username, encodedHashPass, firstName, lastName);
    }

    @Override
    public void insertFriendRequest(String senderUsername, String targetUser) throws HTTPException {

    }

    @Override
    public boolean areUsersFriends(String username1, String username2) throws StorageException {
        return false;
    }

    @Override
    public Set<UserInfo> getFriendRequestsFor(String username) throws StorageException {
        return null;
    }

    @Override
    public Set<UserInfo> getFriendsFor(String username) throws StorageException {
        return null;
    }

    @Override
    public void createFriendship(String frAcceptor, String frSender) throws StorageException, FriendshipException {

    }

    @Override
    public void removeFriend(String removerUsername, String removeeUsername) throws StorageException {

    }
}
