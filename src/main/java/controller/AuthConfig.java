package controller;

import authentication.AuthenticationService;

/**
 * Created by Matthew on 9/7/2015.
 */
public class AuthConfig {
    public static final String DEFAULT_HASH_ALGO = "SHA-256";
    public static final String DEFULT_ENCODING = "UTF-8";

    private String hashAlgo;
    private String encoding;

    public AuthConfig() {
        this(DEFAULT_HASH_ALGO, DEFULT_ENCODING);
    }
    public AuthConfig(String hashAlgo, String encoding){
        this.hashAlgo = hashAlgo;
        this.encoding = encoding;
    }

    public String getHashAlgo(){
        return hashAlgo;
    }
    public String getEncoding(){
        return encoding;
    }
}
