package com.ttkhnvv.rtm.validation.albumartist;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@NotNull(message = "Order is required")
@Min(value = 0, message = "Order must be at least 0")
@Constraint(validatedBy = {})
public @interface ValidOrder {
    String message() default "Invalid order";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
