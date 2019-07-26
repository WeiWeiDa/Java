package com.code.aop;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

/**
 * @author hengj
 * @version MyAgent, v 0.1 2019/7/26 10:55
 */
public class MyAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        ClassFileTransformer transformer = new MyTransformer();
        instrumentation.addTransformer(transformer);
    }
}
