package com.store.exception;

// Thrown when a required setting is missing or not usable.
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }
}