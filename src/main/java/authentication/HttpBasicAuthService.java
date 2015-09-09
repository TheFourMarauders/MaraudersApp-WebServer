package authentication;

import controller.AuthConfig;
import controller.ServiceController;

import storage.StorageService;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
/**
 * Created by Matthew on 9/7/2015.
 */
public class HttpBasicAuthService implements AuthenticationService{
    private static HttpBasicAuthService instance;

    public static HttpBasicAuthService getInstance() {
        if (instance == null) {
            instance = new HttpBasicAuthService();
        }
        return instance;
    }

    private HttpBasicAuthService() {
    }

    @Override
    public void authenticate(String authToken) throws AuthenticationException {
        Credentials cred = getUsernamePassword(authToken);
        String username = cred.getUsername();
        String password = cred.getPassword();
        try {
            AuthConfig authConfig = ServiceController.getInstance().getAuthConfig();
            MessageDigest digest = MessageDigest.getInstance(authConfig.getHashAlgo());
            byte[] hashedPassword = digest.digest(password.getBytes(authConfig.getEncoding()));
            StorageService storageService = ServiceController.getInstance().getStorageService();
            byte[] storedPassword = storageService.getHashedPassword(username);
            if (storedPassword == null || storedPassword.length != hashedPassword.length
                    || !Arrays.equals(hashedPassword, storedPassword)) {
                throw new AuthenticationException("Invalid credentials", 401);
            }
        } catch (java.security.NoSuchAlgorithmException | java.io.UnsupportedEncodingException ex) {
            //TODO: Log error
            throw new AuthenticationException(ex.getMessage(), 500);

        }
    }

    @Override
    public void authenticateUserAcess(String authToken, String targetUser) throws AuthenticationException{
        Credentials cred = getUsernamePassword(authToken);
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
