package com.cinetix.common.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource) {
        super("RESOURCE_NOT_FOUND", resource + " not found: ");
    }
}
