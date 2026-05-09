package com.ttkhnvv.rtm.security.constraint;

import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasRoleAny {
    String message() default "Granted for anyone.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
