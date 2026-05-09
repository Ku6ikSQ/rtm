package com.ttkhnvv.rtm.exception.handler;

import com.ttkhnvv.rtm.exception.genre.GenreNotFoundException;
import com.ttkhnvv.rtm.exception.genre.GenreSlugAlreadyTakenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GenreExceptionHandler {
    @ExceptionHandler(GenreNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleGenreNotFound(GenreNotFoundException e) {
        log.warn("Genre not found: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Genre Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(GenreSlugAlreadyTakenException.class)
    public ResponseEntity<ProblemDetail> handleSlugAlreadyTaken(GenreSlugAlreadyTakenException e) {
        log.warn("Genre slug already taken: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Slug Already Taken");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
