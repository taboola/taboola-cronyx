package com.taboola.cronyx.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CronyxServiceConfig.class)
public class CronyxService {

    public static void main(String[] args) {

        SpringApplication.run(CronyxService.class, args);
    }
}
