package com.zerotohero.web;

import com.zerotohero.core.GreetingService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Module 06: the building blocks of a REST controller, one per method. */
@RestController
@RequestMapping("/api/hello")
public class HelloController {

    private final GreetingService greetingService;

    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    /** Query parameter with a default: GET /api/hello?name=Asha */
    @GetMapping
    public Map<String, String> hello(@RequestParam(defaultValue = "World") String name) {
        return Map.of("message", greetingService.greet(name));
    }

    /** Path variable: GET /api/hello/Asha */
    @GetMapping("/{name}")
    public Map<String, String> helloPath(@PathVariable String name) {
        return Map.of("message", greetingService.greet(name));
    }

    /** Request header: GET /api/hello/lang with Accept-Language: hi */
    @GetMapping("/lang")
    public Map<String, String> helloLang(@RequestHeader(name = "Accept-Language", defaultValue = "en") String lang,
                                         @RequestParam(defaultValue = "World") String name) {
        String message = lang.startsWith("hi") ? greetingService.greetInHindi(name) : greetingService.greet(name);
        return Map.of("message", message);
    }

    public record EchoRequest(String text) {
    }

    /** JSON request body and an explicit status code: POST /api/hello/echo */
    @PostMapping("/echo")
    @ResponseStatus(HttpStatus.CREATED)
    public EchoRequest echo(@RequestBody EchoRequest request) {
        return request;
    }

    /** ResponseEntity gives full control over status, headers and body. */
    @GetMapping("/teapot")
    public ResponseEntity<String> teapot() {
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).header("X-Course", "zero-to-hero").body("I'm a teapot");
    }
}
