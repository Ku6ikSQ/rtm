package com.ttkhnvv.rtm.validation.album;

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
@NotBlank(message = "Album title is required")
@Size(min = 1, max = 255, message = "Album title must be between 1 and 255 characters")
@Constraint(validatedBy = {})
public @interface ValidAlbumTitle {
    String message() default "Invalid album title";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
