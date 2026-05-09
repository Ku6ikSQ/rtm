package com.ttkhnvv.rtm.validation.track;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@NotNull(message = "Track number is required")
@Min(value = 1, message = "Track number must be at least 1")
@Constraint(validatedBy = {})
public @interface ValidTrackNumber {
    String message() default "Invalid track number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
