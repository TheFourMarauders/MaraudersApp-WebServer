package authentication;

import controller.HTTPException;

/**
 * Created by Matthew on 9/9/2015.
 */
public class AuthenticationException extends HTTPException {

    public AuthenticationException(String message, int httpErrorCode) {
        super(message, httpErrorCode);
    }
}
