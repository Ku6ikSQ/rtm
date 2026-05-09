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
@Size(max = 100, message = "Real name must not exceed 100 characters")
@Constraint(validatedBy = {})
public @interface ValidRealName {
    String message() default "Invalid real name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
