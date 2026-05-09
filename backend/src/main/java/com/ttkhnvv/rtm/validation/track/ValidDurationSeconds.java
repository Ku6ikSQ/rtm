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
@NotNull(message = "Duration is required")
@Min(value = 1, message = "Duration must be at least 1 second")
@Constraint(validatedBy = {})
public @interface ValidDurationSeconds {
    String message() default "Invalid duration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
