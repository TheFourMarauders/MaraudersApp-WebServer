package authentication;

import controller.AuthConfig;
import controller.ServiceController;
import spark.Request;
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
    public boolean authenticate(Request request) {
        //Grab user:password
        // convert Base64 to string
        // check storage
        // if it matches, we are good

        String authHeader = request.headers("Authorization");
        if (authHeader == null){
            return false;
        }
        String[] splitHeader = authHeader.split("\\s");
        if(splitHeader.length != 2) {
            return false;
        }
        if(!splitHeader[0].equalsIgnoreCase("Basic")) {
            return false;
        }
        String base64Decoded = new String(Base64.getDecoder().decode(splitHeader[1]));
        String[] splitCredentials = base64Decoded.split(":");
        if(splitCredentials.length != 2) {
            return false;
        }
        String username = splitCredentials[0];
        String password = splitCredentials[1];
        try {
            AuthConfig authConfig = ServiceController.getInstance().getAuthConfig();
            MessageDigest digest = MessageDigest.getInstance(authConfig.getHashAlgo());
            byte[] hashedPassword = digest.digest(password.getBytes(authConfig.getEncoding()));
            StorageService storageService = ServiceController.getInstance().getStorageService();
            byte[] storedPassword = storageService.getHashedPassword(username);
            if (storedPassword == null || storedPassword.length != hashedPassword.length){
                return false;
            }
            return Arrays.equals(hashedPassword, storedPassword);
        } catch (java.security.NoSuchAlgorithmException | java.io.UnsupportedEncodingException ex) {
            //TODO: Log error
            return false;
        }
        return false;
    }
}
