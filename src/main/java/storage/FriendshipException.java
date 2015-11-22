package storage;

import controller.HTTPException;

/**
 * Thrown if two users are not friends. The HTTP error code is 400 Bad Request
 */
public class FriendshipException extends HTTPException {
    private static final int HTTP_ERROR_CODE = 400;

    public FriendshipException(String message) {
        super(message, HTTP_ERROR_CODE);
    }
}
