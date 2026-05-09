package com.ttkhnvv.rtm.validation.artist;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Size(max = 5000, message = "Bio must not exceed 5000 characters")
@Constraint(validatedBy = {})
public @interface ValidBio {
    String message() default "Invalid bio";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
