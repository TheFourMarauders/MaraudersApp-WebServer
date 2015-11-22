package storage;

/**
 * Exception representing duplicated usernames
 * @author Matt and Joe
 */
public class UserAlreadyExistsException extends StorageException {

    /**
     * no arg constructor
     */
    public UserAlreadyExistsException() {
        super("Username is already in use");
    }
}
