package authentication;

import controller.HTTPException;

/**
 * Created by Matthew on 9/7/2015.
 */
public interface AuthenticationService {


    /**
     * Checks to see if the given auth token validates correctly, otherwise throws an exception
     * @param authToken
     * @throws HTTPException
     */
    void authenticate(String authToken) throws HTTPException;

    /**
     * Validates a given token allows access a friends data
     * @param authToken
     * @param targetUser
     * @throws HTTPException
     */
    void validateFriendAccess(String authToken, String targetUser) throws HTTPException;

    /**
     * Validates a given token if it allows access to a groups data
     * @param authtoken
     * @param id
     * @throws HTTPException
     */
    void validateGroupAccess(String authtoken, String id) throws HTTPException;

    /**
     * Validates who the person claims that they are
     * @param authToken
     * @param username
     * @throws AuthenticationException
     */
    void validate(String authToken, String username) throws AuthenticationException;


    /**
     * Retrieves the username corresponding to the authorization token
     * @param authtoken
     * @return
     * @throws AuthenticationException
     */
    String getUsernameFromAuthToken(String authtoken) throws AuthenticationException;
}
