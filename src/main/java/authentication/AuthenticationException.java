package authentication;

import controller.HTTPException;

/**
 * AuthenticationException is a subclass of exception that has an error code and a message
 *
 * @author thefourmarauders
 * @version 1
 */
public class AuthenticationException extends HTTPException {

    /**
     * Constructs an authentication exception given a message and an error code
     * @param message
     * @param httpErrorCode
     */
    public AuthenticationException(String message, int httpErrorCode) {
        super(message, httpErrorCode);
    }
}
