package me.hao0.trace.core.util;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public final class Yamls {

    private static final Yaml yaml = new Yaml();

    private Yamls(){}

    public static <T> T load(InputStream in, Class<T> clazz){
        return yaml.loadAs(in, clazz);
    }
}
