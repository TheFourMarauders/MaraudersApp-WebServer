package storage;

/**
 * Created by Joe on 9/10/2015.
 */
public class NoSuchUserException extends StorageException {
    public NoSuchUserException(String username) {
        super("No user found matching: " + username);
    }
}
