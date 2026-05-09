package com.ttkhnvv.rtm.exception.handler;

import com.ttkhnvv.rtm.exception.platform.PlatformNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class PlatformExceptionHandler {
    @ExceptionHandler(PlatformNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePlatformNotFound(PlatformNotFoundException e) {
        log.warn("Platform not found: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Platform Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }
}
