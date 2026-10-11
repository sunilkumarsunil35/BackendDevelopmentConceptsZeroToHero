package com.zerotohero.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.zerotohero.core.BeanScopesConfig.TaskCounter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Module 04: start a small Spring container with only the beans under test. ApplicationContextRunner
 * is fast and shows exactly what the container does: create beans, inject them, bind properties.
 */
class CoreConceptsTest {

    @Configuration
    @EnableConfigurationProperties(GreetingProperties.class)
    @Import({EnglishGreeter.class, HindiGreeter.class, GreetingService.class, BeanScopesConfig.class,
            LifecycleDemo.class})
    static class CoreConfig {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CoreConfig.class);

    @Test
    void injectsThePrimaryGreeterByDefaultAndTheQualifiedOneOnRequest() {
        runner.run(ctx -> {
            GreetingService service = ctx.getBean(GreetingService.class);
            assertThat(service.greet("Asha")).isEqualTo("Hello, Asha!");
            assertThat(service.greetInHindi("Asha")).isEqualTo("Namaste, Asha!");
            assertThat(service.greetWith("hindi", "Ravi")).isEqualTo("Namaste, Ravi!");
        });
    }

    @Test
    void bindsConfigurationProperties() {
        runner.withPropertyValues("app.greeting.prefix=Hi", "app.greeting.exclaim=false")
                .run(ctx -> assertThat(ctx.getBean(GreetingService.class).greet("Asha")).isEqualTo("Hi, Asha."));
    }

    @Test
    void failsFastOnInvalidConfiguration() {
        runner.withPropertyValues("app.greeting.prefix= ")
                .run(ctx -> assertThat(ctx).hasFailed());
    }

    @Test
    void singletonAndPrototypeScopes() {
        runner.run(ctx -> {
            assertThat(ctx.getBean(java.time.Clock.class)).isSameAs(ctx.getBean(java.time.Clock.class));
            TaskCounter a = ctx.getBean(TaskCounter.class);
            TaskCounter b = ctx.getBean(TaskCounter.class);
            assertThat(a).isNotSameAs(b);
            a.increment();
            assertThat(b.increment()).isEqualTo(1);
        });
    }

    @Test
    void runsLifecycleCallbacks() {
        runner.run(ctx -> assertThat(ctx.getBean(LifecycleDemo.class).isReady()).isTrue());
    }
}
