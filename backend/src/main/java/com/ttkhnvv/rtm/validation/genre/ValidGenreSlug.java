package com.ttkhnvv.rtm.validation.genre;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "Slug is required")
@Size(min = 1, max = 100, message = "Slug must be between 1 and 100 characters")
@Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain only lowercase letters, digits, and hyphens")
@Constraint(validatedBy = {})
public @interface ValidGenreSlug {
    String message() default "Invalid genre slug";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
