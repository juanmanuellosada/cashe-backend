package com.cashe.backend.validation.constraint;

import com.cashe.backend.validation.validator.EitherAccountIdOrCardIdValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = EitherAccountIdOrCardIdValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EitherAccountIdOrCardId {
    String message() default "Either accountId or cardId must be provided, but not both";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}