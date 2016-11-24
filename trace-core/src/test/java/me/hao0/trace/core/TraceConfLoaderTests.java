package me.hao0.trace.core;

import me.hao0.trace.core.config.TraceConf;
import me.hao0.trace.core.config.TraceConfLoader;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class TraceConfLoaderTests {

    @Test
    public void testLoad(){
        TraceConf conf = TraceConfLoader.load("trace.yml");
        assertNotNull(conf);
        System.out.println(conf);
    }
}
