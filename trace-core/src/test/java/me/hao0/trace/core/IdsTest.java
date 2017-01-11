package me.hao0.trace.core;

import me.hao0.trace.core.util.OldIds;
import me.hao0.trace.core.util.Ids;
import org.junit.Test;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class IdsTest {

    @Test
    public void testGet() throws InterruptedException {
        for (;;){
            try {
                System.out.println(OldIds.get());
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("over");
                System.exit(1);
            }
        }
    }

    @Test
    public void testGet2() throws InterruptedException {
        for (;;){
            try {
                System.out.println(Ids.get());
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("over");
                System.exit(1);
            }
        }
    }
}
