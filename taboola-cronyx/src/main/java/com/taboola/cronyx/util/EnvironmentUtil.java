package com.taboola.cronyx.util;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Properties;

public abstract class EnvironmentUtil {


    public static Properties findProps(ConfigurableEnvironment env, String prefix){
        Properties props = new Properties();

        for (PropertySource<?> source : env.getPropertySources()) {
            if (source instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) source;
                for (String name : enumerable.getPropertyNames()) {
                    if (name.startsWith(prefix)) {
                        props.putIfAbsent(name, enumerable.getProperty(name));
                    }
                }

            }
        }

        return props;
    }


    public static Properties changePrefix(Properties source, String prefix, String newPrefix){

        final Properties ret = new Properties();

        source.entrySet()
                .stream()
                .forEach(ent -> ret.put(((String)ent.getKey()).replaceAll("^"+prefix, newPrefix), ent.getValue()));

        return ret;
    }
}
