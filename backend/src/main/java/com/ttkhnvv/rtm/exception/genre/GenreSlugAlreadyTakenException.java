package com.ttkhnvv.rtm.exception.genre;

public class GenreSlugAlreadyTakenException extends RuntimeException {
    public GenreSlugAlreadyTakenException(String message) {
        super(message);
    }
}
