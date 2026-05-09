package com.ttkhnvv.rtm.exception.handler;

import com.ttkhnvv.rtm.exception.albumgenre.AlbumGenreAlreadyExistsException;
import com.ttkhnvv.rtm.exception.albumgenre.AlbumGenreNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AlbumGenreExceptionHandler {
    @ExceptionHandler(AlbumGenreNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(AlbumGenreNotFoundException e) {
        log.warn("AlbumGenre not found: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Album Genre Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(AlbumGenreAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleAlreadyExists(AlbumGenreAlreadyExistsException e) {
        log.warn("AlbumGenre already exists: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Album Genre Already Exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
