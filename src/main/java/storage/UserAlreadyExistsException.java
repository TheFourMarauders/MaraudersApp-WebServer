package storage;

/**
 * Created by Joe on 9/11/2015.
 */
public class UserAlreadyExistsException extends StorageException {

    public UserAlreadyExistsException() {
        super("Username is already in use");
    }
}
