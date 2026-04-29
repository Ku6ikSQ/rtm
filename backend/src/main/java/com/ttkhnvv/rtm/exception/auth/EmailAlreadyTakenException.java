package com.ttkhnvv.rtm.exception.auth;

public class EmailAlreadyTakenException extends RuntimeException {
    public EmailAlreadyTakenException(String emailAlreadyTaken) {
        super(emailAlreadyTaken);
    }
}
