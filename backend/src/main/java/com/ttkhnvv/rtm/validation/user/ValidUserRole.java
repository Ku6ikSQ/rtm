package com.ttkhnvv.rtm.validation.user;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@NotNull(message = "Role is required")
@Constraint(validatedBy = {})
public @interface ValidUserRole {
    String message() default "Invalid role";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}