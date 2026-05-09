package com.ttkhnvv.rtm.validation.album;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@NotNull(message = "Release year is required")
@Min(value = 1000, message = "Release year must be no earlier than 1000")
@Max(value = 2100, message = "Release year must be no later than 2100")
@Constraint(validatedBy = {})
public @interface ValidReleaseYear {
    String message() default "Invalid release year";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
