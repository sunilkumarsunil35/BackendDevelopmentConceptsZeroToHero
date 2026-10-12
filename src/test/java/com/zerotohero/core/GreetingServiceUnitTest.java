package com.zerotohero.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Module 04: constructor injection means you can test without Spring. Pass in lambdas as fakes. */
class GreetingServiceUnitTest {

    @Test
    void worksWithHandWrittenFakes() {
        Greeter loud = name -> "HEY " + name.toUpperCase();
        Greeter quiet = name -> "hi " + name;
        GreetingService service = new GreetingService(loud, quiet, Map.of("loud", loud));
        assertThat(service.greet("asha")).isEqualTo("HEY ASHA");
        assertThat(service.greetInHindi("asha")).isEqualTo("hi asha");
        assertThatThrownBy(() -> service.greetWith("missing", "x")).isInstanceOf(IllegalArgumentException.class);
    }
}
