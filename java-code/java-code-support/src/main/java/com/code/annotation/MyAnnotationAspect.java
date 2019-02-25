package com.code.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author hengjian
 * @date 2019/2/25
 */
@Aspect
@Component
public class MyAnnotationAspect {
    @Around("@annotation(myAnnotation)")
    public Object around(ProceedingJoinPoint point, MyAnnotation myAnnotation) throws Throwable {
        Object[] args = point.getArgs();
        String className = point.getSignature().getDeclaringTypeName();
        String methodName = point.getSignature().getName();
        MyMethodInvoker myMethodInvoker = ApplicationContextHolder.getContext().getBean(myAnnotation.methodInvoker());
        myMethodInvoker.invoke(args);
        Object result = point.proceed();
        return result;
    }
}
