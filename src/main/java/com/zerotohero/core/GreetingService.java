package com.zerotohero.core;

import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** Module 04: a service that receives its collaborators through the constructor. */
@Service
public class GreetingService {

    private final Greeter defaultGreeter;
    private final Greeter hindiGreeter;
    private final Map<String, Greeter> allGreeters;

    public GreetingService(Greeter defaultGreeter,
                           @Qualifier("hindi") Greeter hindiGreeter,
                           Map<String, Greeter> allGreeters) {
        this.defaultGreeter = defaultGreeter;
        this.hindiGreeter = hindiGreeter;
        // Spring can inject every bean of a type as a Map keyed by bean name.
        this.allGreeters = allGreeters;
    }

    public String greet(String name) {
        return defaultGreeter.greet(name);
    }

    public String greetInHindi(String name) {
        return hindiGreeter.greet(name);
    }

    public String greetWith(String greeterBean, String name) {
        Greeter g = allGreeters.get(greeterBean);
        if (g == null) throw new IllegalArgumentException("Unknown greeter: " + greeterBean);
        return g.greet(name);
    }
}
