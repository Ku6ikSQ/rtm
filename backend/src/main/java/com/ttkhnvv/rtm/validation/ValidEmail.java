package com.ttkhnvv.rtm.validation;


import jakarta.validation.Constraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "Email is required")
@Email(message = "Invalid email format")
@Size(max = 255, message = "Email must not exceed 255 characters")
@Constraint(validatedBy = {})
public @interface ValidEmail {
    String message() default "Invalid email";
    Class<?>[] groups() default {};
    Class<? extends jakarta.validation.Payload>[] payload() default {};
}
