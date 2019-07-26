package com.code.aop;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author hengj
 * @version MyTransformer, v 0.1 2019/7/26 10:49
 */
public class MyTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("==============" + MyTransformer.class.getSimpleName() + "==============");
        System.out.println("ClassLoader:" + loader.getClass());
        System.out.println("ClassName:" + className);
        return null;
    }
}
