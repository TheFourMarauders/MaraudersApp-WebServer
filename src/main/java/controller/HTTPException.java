package controller;

/**
 * Created by Joe on 9/10/2015.
 */
public class HTTPException extends Exception {
    private int httpErrorCode;

    public HTTPException(String message, int httpErrorCode) {
        super(message);
        this.httpErrorCode = httpErrorCode;
    }
    public int getHttpErrorCode() {
        return httpErrorCode;
    }
}
