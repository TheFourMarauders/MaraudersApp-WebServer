package controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Matthew on 9/7/2015.
 */
public class AuthConfig {
    public static final String DEFAULT_AUTH_TYPE = "HTTPBasic";
    public static final String DEFAULT_HASH_ALGO = "SHA-256";
    public static final String DEFULT_ENCODING = "UTF-8";

    private String type;

    private AuthType authType;

    private String hashAlgo;
    private String encoding;

    public AuthConfig() {
        this(DEFAULT_AUTH_TYPE, DEFAULT_HASH_ALGO, DEFULT_ENCODING);
    }

    @JsonCreator
    public AuthConfig(
            @JsonProperty("type") String authType,
            @JsonProperty("algo") String hashAlgo,
            @JsonProperty("encoding") String encoding) {
        this.type = authType;
        this.hashAlgo = hashAlgo;
        this.encoding = encoding;
    }

    public String getType() {
        return type;
    }
    public String getHashAlgo(){
        return hashAlgo;
    }
    public String getEncoding(){
        return encoding;
    }

    public void setAuthType(AuthType a) {
        authType = a;
    }

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
