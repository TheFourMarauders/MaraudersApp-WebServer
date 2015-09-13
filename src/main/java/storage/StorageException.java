package storage;

import controller.HTTPException;

/**
 * Created by Joe on 9/9/2015.
 */
public class StorageException extends HTTPException {
    public StorageException(String message) {
        super(message, 500);
    }
}
