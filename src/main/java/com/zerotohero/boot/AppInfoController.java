package com.zerotohero.boot;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Module 05: read configuration with {@code @Value} and inspect active profiles via {@link Environment}. */
@RestController
public class AppInfoController {

    private final String appName;
    private final Environment environment;

    public AppInfoController(@Value("${spring.application.name}") String appName, Environment environment) {
        this.appName = appName;
        this.environment = environment;
    }

    public record AppInfo(String name, List<String> activeProfiles, String javaVersion) {
    }

    @GetMapping("/api/info")
    public AppInfo info() {
        return new AppInfo(appName, Arrays.asList(environment.getActiveProfiles()), System.getProperty("java.version"));
    }
}
