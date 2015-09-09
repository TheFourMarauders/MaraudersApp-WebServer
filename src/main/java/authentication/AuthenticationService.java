package authentication;

import spark.Request;
/**
 * Created by Matthew on 9/7/2015.
 */
public interface AuthenticationService {
    void authenticate(String authToken) throws AuthenticationException;
    void authenticateUserAcess(String authToken, String targetUser) throws AuthenticationException;
    //boolean authenticateGroupAcess(String authToken, String targetGroup);
}
