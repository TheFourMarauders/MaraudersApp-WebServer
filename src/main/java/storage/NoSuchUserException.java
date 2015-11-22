package storage;

import controller.HTTPException;

/**
 * Exception thrown when no such user exists in the system
 */
public class NoSuchUserException extends HTTPException {
    /**
     * Constructor that takes in a string username
     * @param username
     */
    public NoSuchUserException(String username) {
        super("No user found matching: " + username, 400);
    }
}
