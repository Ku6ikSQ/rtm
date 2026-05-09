package com.ttkhnvv.rtm.validation.common;

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
@NotBlank(message = "Token is required")
@Size(max = 512, message = "Token is too long")
@Constraint(validatedBy = {})
public @interface ValidToken {
    String message() default "Invalid token";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}