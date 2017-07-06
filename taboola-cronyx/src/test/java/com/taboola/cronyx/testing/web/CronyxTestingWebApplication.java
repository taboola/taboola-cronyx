package com.taboola.cronyx.testing.web;

import com.taboola.cronyx.testing.TransientCronyxConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TransientCronyxConfiguration.class)
public class CronyxTestingWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CronyxTestingWebApplication.class, args);
    }
}
