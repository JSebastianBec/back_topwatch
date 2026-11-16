package com.topwatch.back_topwatch.exception;

/** The resource can't be removed because other data still depends on it. */
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }

}
