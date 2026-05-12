package com.ttkhnvv.rtm.exception.handler;

import com.ttkhnvv.rtm.exception.albumlink.AlbumLinkAlreadyExistsException;
import com.ttkhnvv.rtm.exception.albumlink.AlbumLinkNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AlbumLinkExceptionHandler {
    @ExceptionHandler(AlbumLinkNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(AlbumLinkNotFoundException e) {
        log.warn("AlbumLink not found: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Album Link Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(AlbumLinkAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleAlreadyExists(AlbumLinkAlreadyExistsException e) {
        log.warn("AlbumLink already exists: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Album Link Already Exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}