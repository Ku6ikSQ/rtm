package com.ttkhnvv.rtm.security.constraint;

import jakarta.validation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ADMIN')")
public @interface HasRoleAdmin {
    String message() default "Granted for admin only.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
