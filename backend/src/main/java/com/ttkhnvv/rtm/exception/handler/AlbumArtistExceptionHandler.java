package com.ttkhnvv.rtm.exception.handler;

import com.ttkhnvv.rtm.exception.albumartist.AlbumArtistAlreadyExistsException;
import com.ttkhnvv.rtm.exception.albumartist.AlbumArtistNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AlbumArtistExceptionHandler {
    @ExceptionHandler(AlbumArtistNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(AlbumArtistNotFoundException e) {
        log.warn("AlbumArtist not found: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Album Artist Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(AlbumArtistAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleAlreadyExists(AlbumArtistAlreadyExistsException e) {
        log.warn("AlbumArtist already exists: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Album Artist Already Exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
