package com.taboola.cronyx.builtin.jobs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {BuiltInJobBundle.class})
public class BuiltInJobBundle {
}
