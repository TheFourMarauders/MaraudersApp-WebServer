package controller;

/**
 * An exception that gets passed to the web server and sent as a response
 * via one of the endpoints if something goes wrong. The error corresponds
 * to an http status code
 *
 * @author Joe
 * @version 1.0
 */
public class HTTPException extends Exception {
    private int httpErrorCode;

    /**
     * Constructs an exception with the given message and status code
     * @param message
     * @param httpErrorCode
     */
    public HTTPException(String message, int httpErrorCode) {
        super(message);
        this.httpErrorCode = httpErrorCode;
    }

    /**
     *
     * @return  the error code/status code
     */
    public int getHttpErrorCode() {
        return httpErrorCode;
    }
}
