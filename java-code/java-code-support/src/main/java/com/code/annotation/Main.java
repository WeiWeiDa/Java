package com.code.annotation;

/**
 * @author hengjian
 * @date 2019/2/25
 */
public class Main {
    @MyAnnotation(methodInvoker = PrintMethodInvoker.class)
    public void testAnnotation() {

    }
}
