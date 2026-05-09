package com.ttkhnvv.rtm.validation.review;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Size(max = 10000, message = "Review text must not exceed 10000 characters")
@Constraint(validatedBy = {})
public @interface ValidReviewText {
    String message() default "Invalid review text";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
