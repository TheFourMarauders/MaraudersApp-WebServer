package authentication;

/**
 * Created by Matthew on 9/9/2015.
 */
public class AuthenticationException extends Exception{
    private int httpErrorCode;

    public AuthenticationException(String message, int httpErrorCode) {
        super(message);
        this.httpErrorCode = httpErrorCode;
    }
    public int getHttpErrorCode() {
        return httpErrorCode;
    }
}
