package storage;

import controller.HTTPException;

/**
 * Created by Joe on 9/10/2015.
 */
public class FriendshipException extends HTTPException {
    private static final int HTTP_ERROR_CODE = 400;

    public FriendshipException(String message) {
        super(message, HTTP_ERROR_CODE);
    }
}
