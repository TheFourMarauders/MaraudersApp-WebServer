package storage;

/**
 * Created by Matthew on 9/7/2015.
 */
public interface StorageService {
    byte[] getHashedPassword(String username);

    boolean createUser(String username, String hashedPassword, String firstName, String lastName);
}
