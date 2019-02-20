package com.code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;

/**
 * @author hengjian
 * @date 2019/2/20
 */
public class JavaCodeLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaCodeLauncher.class);

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/spring-main.xml");
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("JavaCodeLauncher started successfully!");
        }
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage(), e);
            }
        } finally {

        }
    }
}
