package com.zerotohero.core;

import org.springframework.stereotype.Component;

/** Module 04: a second implementation, chosen explicitly with {@code @Qualifier("hindi")}. */
@Component("hindi")
public class HindiGreeter implements Greeter {

    @Override
    public String greet(String name) {
        return "Namaste, " + name + "!";
    }
}
