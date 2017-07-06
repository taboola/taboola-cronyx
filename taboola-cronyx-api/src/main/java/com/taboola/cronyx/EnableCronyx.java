package com.taboola.cronyx;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.*;

/**
 * Enables Cronyx by importing CronyxAutoConfiguration into the current context.
 * depends that cronyx core exists in the runtime classpath.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableCronyx.InputSelector.class)
public @interface EnableCronyx {

    /**
     * This class  purpose is to a runtime only dependency on CronyxAutoConfiguration
     */
    class InputSelector implements ImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            return new String[]{"com.taboola.cronyx.autoconfigure.CronyxAutoConfiguration"};
        }
    }

}
