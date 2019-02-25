package com.code.annotation;

import java.lang.annotation.*;

/**
 * @author hengjian
 * @date 2019/2/25
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAnnotation {
    Class<? extends MyMethodInvoker> methodInvoker();
}
