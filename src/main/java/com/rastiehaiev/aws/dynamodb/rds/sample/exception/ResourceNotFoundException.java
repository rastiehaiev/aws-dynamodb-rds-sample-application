package com.rastiehaiev.aws.dynamodb.rds.sample.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
