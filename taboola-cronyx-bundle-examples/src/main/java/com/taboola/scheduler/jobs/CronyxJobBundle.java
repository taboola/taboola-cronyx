package com.taboola.scheduler.jobs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {CronyxJobBundle.class})
public class CronyxJobBundle {
}
