package authentication;

import controller.AuthConfig;
import controller.ServiceController;

import storage.StorageException;
import storage.StorageService;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
/**
 * Created by Matthew on 9/7/2015.
 */
public class HttpBasicAuthService implements AuthenticationService{

    private AuthConfig authConfig;
    private StorageService storageService;

    public HttpBasicAuthService(AuthConfig authConfig, StorageService storageService) {
        this.authConfig = authConfig;
        this.storageService = storageService;
    }

    @Override
    public void authenticate(String authToken) throws AuthenticationException {
        Credentials cred = getUsernamePassword(authToken);
        String username = cred.getUsername();
        String password = cred.getPassword();
        try {
            MessageDigest digest = MessageDigest.getInstance(authConfig.getHashAlgo());
            byte[] hashedPassword = digest.digest(password.getBytes(authConfig.getEncoding()));
            byte[] storedPassword = storageService.getHashedPassword(username);
            if (storedPassword == null || storedPassword.length != hashedPassword.length
                    || !Arrays.equals(hashedPassword, storedPassword)) {
                throw new AuthenticationException("Invalid credentials", 401);
            }
        } catch (java.security.NoSuchAlgorithmException | java.io.UnsupportedEncodingException ex) {
            //TODO: Log error
            throw new AuthenticationException(ex.getMessage(), 500);

        } catch (StorageException ex) {
            throw new AuthenticationException(ex.getMessage(), 500);
        }
    }

    @Override
    public void validateFriendAccess(String authToken, String targetUser) throws AuthenticationException{
        Credentials cred = getUsernamePassword(authToken);
        if(targetUser == null || targetUser.isEmpty()){
            throw new AuthenticationException("Invalid target username", 400);
        }

        try {
            if (storageService.areUsersFriends(cred.username, targetUser)) {
                throw new AuthenticationException("Not friend of user", 403);
            }
        } catch (StorageException e) {
            throw new AuthenticationException("Data retrieval error", 500);
        }
    }

    @Override
    public void validate(String authToken, String username) throws AuthenticationException {
        Credentials cred = getUsernamePassword(authToken);
        if (!(cred.username.equals(username))) {
            throw new AuthenticationException("Forbidden", 403);
        }
    }

    private Credentials getUsernamePassword(String authToken) throws AuthenticationException{
        if (authToken == null || authToken.isEmpty()) {
            throw new AuthenticationException("Empty authorization", 401);
        }
        String[] splitHeader = authToken.split("\\s");
        if (splitHeader.length != 2 || !splitHeader[0].equalsIgnoreCase("Basic")) {
            throw new AuthenticationException("Incorrect Formatting for HTTP Basic Authentication", 401);
        }
        String base64Decoded = new String(Base64.getDecoder().decode(splitHeader[1]));
        String[] splitCredentials = base64Decoded.split(":");
        if (splitCredentials.length != 2) {
            throw new AuthenticationException("Incorrect Formatting for HTTP Basic Authentication", 401);
        }
        String username = splitCredentials[0];
        String password = splitCredentials[1];
        return new Credentials(username, password);
    }

    private class Credentials {
        private String username;
        private String password;

        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

}
