package com.zerotohero.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Module 05: run code once the application has started. Only active outside the "test" profile. */
@Component
@Profile("!test")
public class StartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    @Override
    public void run(ApplicationArguments args) {
        log.info("Bookstore API is up. Try GET http://localhost:8080/api/books or open /swagger-ui.html");
    }
}
