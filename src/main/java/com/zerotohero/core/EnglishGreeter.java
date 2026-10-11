package com.zerotohero.core;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Module 04: {@code @Primary} makes this the default when more than one Greeter exists. */
@Component
@Primary
public class EnglishGreeter implements Greeter {

    private final GreetingProperties properties;

    // Constructor injection: Spring passes the dependency in. No @Autowired needed with one constructor.
    public EnglishGreeter(GreetingProperties properties) {
        this.properties = properties;
    }

    @Override
    public String greet(String name) {
        return properties.prefix() + ", " + name + (properties.exclaim() ? "!" : ".");
    }
}
