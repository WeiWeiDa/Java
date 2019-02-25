package com.code.annotation;

/**
 * @author hengjian
 * @date 2019/2/25
 */
public class PrintMethodInvoker implements MyMethodInvoker<String> {
    @Override
    public void invoke(String s) {
        System.out.println("PrintMethodInvoker-->" + s);
    }
}
