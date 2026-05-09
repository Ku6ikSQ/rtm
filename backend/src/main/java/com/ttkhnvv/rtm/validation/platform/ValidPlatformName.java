package com.ttkhnvv.rtm.validation.platform;

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
@NotBlank(message = "Platform name is required")
@Size(min = 1, max = 100, message = "Platform name must be between 1 and 100 characters")
@Constraint(validatedBy = {})
public @interface ValidPlatformName {
    String message() default "Invalid platform name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
