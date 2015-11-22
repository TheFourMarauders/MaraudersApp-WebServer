package storage;

import controller.HTTPException;

/**
 * A class representing an exception that happens when a storage error occurs, usually this is caused by an internal
 * error - HTTP error code 500
 */
public class StorageException extends HTTPException {
    /**
     * Constructor in which you can pass an error message
     * @param message
     */
    public StorageException(String message) {
        super(message, 500);
    }
}
