package authentication;

/**
 * Created by Matthew on 9/7/2015.
 */
public interface AuthenticationService {
    void authenticate(String authToken) throws AuthenticationException;

    void validateFriendAccess(String authToken, String targetUser) throws AuthenticationException;

    void validate(String authToken, String username) throws AuthenticationException;
}
