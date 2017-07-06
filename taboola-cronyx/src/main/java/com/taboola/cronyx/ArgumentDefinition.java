package com.taboola.cronyx;

import java.io.Serializable;
import java.lang.annotation.Annotation;

public class ArgumentDefinition implements Serializable {

    private Class<?> type;
    private Annotation descriptor;

    public ArgumentDefinition(Class<?> type, Annotation descriptor) {
        this.type = type;
        this.descriptor = descriptor;
    }

    public Class<?> getType() {
        return type;
    }

    public Annotation getDescriptor() {
        return descriptor;
    }
}
