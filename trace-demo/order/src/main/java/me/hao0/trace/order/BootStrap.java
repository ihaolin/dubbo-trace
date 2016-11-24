package me.hao0.trace.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class BootStrap {

    private static final Logger log = LoggerFactory.getLogger(BootStrap.class);

    private static ClassPathXmlApplicationContext context;

    public void run() throws InterruptedException {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            // add hook here
        });

        context = new ClassPathXmlApplicationContext(
                "classpath:order-context.xml"
        );

        context.start();
        log.info("Order Service Start Ok!");
        while (true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }

    public static void main(String[] args) throws Exception {

        BootStrap server = new BootStrap();
        try {
            server.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
