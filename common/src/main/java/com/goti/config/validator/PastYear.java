package com.goti.config.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PastYearValidator.class)
public @interface PastYear {
	String message() default "{yearName}은(는) 현재보다 미래일 수 없습니다.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
	String yearName() default "연도";
}
