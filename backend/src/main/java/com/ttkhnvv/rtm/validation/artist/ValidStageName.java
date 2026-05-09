package com.ttkhnvv.rtm.validation.artist;

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
@NotBlank(message = "Stage name is required")
@Size(min = 1, max = 100, message = "Stage name must be between 1 and 100 characters")
@Constraint(validatedBy = {})
public @interface ValidStageName {
    String message() default "Invalid stage name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

