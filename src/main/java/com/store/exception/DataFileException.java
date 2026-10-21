package com.store.exception;

// CHECKED (extends Exception): the compiler forces callers to handle file problems,
// because disk and file failures cannot be prevented by the caller.
public class DataFileException extends Exception {

    public DataFileException(String message) {
        super(message);
    }

    public DataFileException(String message, Throwable cause) {
        super(message, cause);
    }
}