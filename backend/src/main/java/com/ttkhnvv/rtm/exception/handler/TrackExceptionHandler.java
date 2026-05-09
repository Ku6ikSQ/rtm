package com.ttkhnvv.rtm.exception.handler;

import com.ttkhnvv.rtm.exception.track.TrackNotFoundException;
import com.ttkhnvv.rtm.exception.track.TrackPositionAlreadyTakenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class TrackExceptionHandler {
    @ExceptionHandler(TrackNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTrackNotFound(TrackNotFoundException e) {
        log.warn("Track not found: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Track Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(TrackPositionAlreadyTakenException.class)
    public ResponseEntity<ProblemDetail> handlePositionAlreadyTaken(TrackPositionAlreadyTakenException e) {
        log.warn("Track position already taken: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Track Position Already Taken");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
