package controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines a configuration for authentication service
 * including the type of service, the hashing algorithm, and the string encoding
 *
 * @author Joe and Matt
 * @version 1.0
 */
public class AuthConfig {
    public static final String DEFAULT_AUTH_TYPE = "HTTPBasic";
    public static final String DEFAULT_HASH_ALGO = "SHA-256";
    public static final String DEFULT_ENCODING = "UTF-8";

    private String type;

    private AuthType authType;

    private String hashAlgo;
    private String encoding;

    /**
     * Constructs with HTTPBasic, SHA-256, and UTF-8
     */
    public AuthConfig() {
        this(DEFAULT_AUTH_TYPE, DEFAULT_HASH_ALGO, DEFULT_ENCODING);
    }

    /**
     *
     * @param authType
     * @param hashAlgo
     * @param encoding
     */
    @JsonCreator
    public AuthConfig(
            @JsonProperty("type") String authType,
            @JsonProperty("algo") String hashAlgo,
            @JsonProperty("encoding") String encoding) {
        this.type = authType;
        this.hashAlgo = hashAlgo;
        this.encoding = encoding;
    }

    /**
     *
     * @return  the type of the desired auth service
     *             currently only "httpbasic" is supported
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public String getHashAlgo(){
        return hashAlgo;
    }

    /**
     *
     * @return
     */
    public String getEncoding(){
        return encoding;
    }

    /**
     *
     * @param a
     */
    public void setAuthType(AuthType a) {
        authType = a;
    }


    /**
     * Enumerates the supported authentication types, currently only HTTPBASIC
     *
     * @author Matt
     */
    public enum AuthType {
        HTTPBASIC;

        public static AuthType verify(String s) {
            if (s.equalsIgnoreCase("httpbasic")) {
                return HTTPBASIC;
            } else {
                throw new IllegalArgumentException("No auth type matches " + s);
            }
        }
    }
}
