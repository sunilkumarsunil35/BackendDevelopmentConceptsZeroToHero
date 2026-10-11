package com.zerotohero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Module 05: the entry point. {@code @SpringBootApplication} is three annotations in one:
 * {@code @Configuration} (this class can declare beans), {@code @EnableAutoConfiguration}
 * (configure beans based on what is on the classpath) and {@code @ComponentScan} (find
 * {@code @Component} classes in this package and below).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ZeroToHeroApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZeroToHeroApplication.class, args);
    }
}
