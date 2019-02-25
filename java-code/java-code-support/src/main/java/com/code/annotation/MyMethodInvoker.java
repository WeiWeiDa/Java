package com.code.annotation;

/**
 * @author hengjian
 * @date 2019/2/25
 */
public interface MyMethodInvoker<T> {
    void invoke(T t);
}
