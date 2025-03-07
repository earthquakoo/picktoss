package com.picktoss.picktossserver.core.swagger;

import com.picktoss.picktossserver.core.exception.ErrorInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExample {

    ErrorInfo value();
}