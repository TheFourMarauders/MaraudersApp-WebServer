package authentication;

import controller.HTTPException;

/**
 * Created by Matthew on 9/7/2015.
 */
public interface AuthenticationService {
    void authenticate(String authToken) throws HTTPException;

    void validateFriendAccess(String authToken, String targetUser) throws HTTPException;

    void validate(String authToken, String username) throws AuthenticationException;
}
