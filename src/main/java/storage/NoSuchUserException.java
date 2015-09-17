package storage;

import controller.HTTPException;

/**
 * Created by Joe on 9/10/2015.
 */
public class NoSuchUserException extends HTTPException {
    public NoSuchUserException(String username) {
        super("No user found matching: " + username, 400);
    }
}
