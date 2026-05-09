package com.ttkhnvv.rtm.validation.genre;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Size(max = 5000, message = "Description must not exceed 5000 characters")
@Constraint(validatedBy = {})
public @interface ValidGenreDescription {
    String message() default "Invalid genre description";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
