package com.rastiehaiev.aws.dynamodb.rds.sample.exception;

public class BadParametersException extends RuntimeException {

    public BadParametersException(String message) {
        super(message);
    }
}
