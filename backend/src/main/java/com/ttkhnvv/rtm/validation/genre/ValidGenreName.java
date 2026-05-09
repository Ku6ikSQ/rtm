package com.ttkhnvv.rtm.validation.genre;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "Genre name is required")
@Size(min = 1, max = 100, message = "Genre name must be between 1 and 100 characters")
@Constraint(validatedBy = {})
public @interface ValidGenreName {
    String message() default "Invalid genre name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
