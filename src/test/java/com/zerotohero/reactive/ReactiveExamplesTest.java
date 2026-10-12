package com.zerotohero.reactive;

import com.zerotohero.reactive.ReactiveExamples.Book;
import com.zerotohero.reactive.ReactiveExamples.Review;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/** Module 22: StepVerifier subscribes and checks each signal in order. */
class ReactiveExamplesTest {

    private final Flux<Book> books = Flux.just(
            new Book("Effective Java", 45.99, "Bloch"),
            new Book("Clean Code", 37.50, "Martin"),
            new Book("Refactoring", 47.80, "Fowler"));

    @Test
    void mapAndFilter() {
        StepVerifier.create(ReactiveExamples.expensiveTitles(books, 45))
                .expectNext("EFFECTIVE JAVA", "REFACTORING")
                .verifyComplete();
    }

    @Test
    void flatMapCallsAnotherSourcePerItem() {
        StepVerifier.create(ReactiveExamples.reviewsFor(books, t -> Flux.just(new Review(t, 5))))
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void zipCombinesTwoResults() {
        StepVerifier.create(ReactiveExamples.bookWithRating(books.next(), Mono.just(4.8)))
                .expectNext("Effective Java rated 4.8")
                .verifyComplete();
    }

    @Test
    void retryThenFallback() {
        AtomicInteger attempts = new AtomicInteger();
        Mono<String> flaky = Mono.fromCallable(() -> {
            if (attempts.incrementAndGet() < 3) throw new IllegalStateException("flaky");
            return "ok";
        });
        StepVerifier.create(ReactiveExamples.withFallback(flaky, 5, "fallback")).expectNext("ok").verifyComplete();

        Mono<String> broken = Mono.error(new IllegalStateException("down"));
        StepVerifier.create(ReactiveExamples.withFallback(broken, 2, "fallback")).expectNext("fallback").verifyComplete();
    }

    @Test
    void timeoutUsesVirtualTimeSoTheTestIsInstant() {
        StepVerifier.withVirtualTime(() -> ReactiveExamples.withTimeout(
                        Mono.just("slow").delayElement(Duration.ofSeconds(10)), Duration.ofSeconds(2), "fallback"))
                .thenAwait(Duration.ofSeconds(2))
                .expectNext("fallback")
                .verifyComplete();
    }

    @Test
    void backpressureRequestsInBatches() {
        StepVerifier.create(ReactiveExamples.inBatches(Flux.range(1, 10), 3), 0)
                .thenRequest(2).expectNext(1, 2)
                .thenRequest(8).expectNextCount(8)
                .verifyComplete();
    }

    @Test
    void collectToList() {
        StepVerifier.create(ReactiveExamples.authors(books.concatWith(Flux.just(new Book("x", 1, "Bloch")))))
                .expectNext(List.of("Bloch", "Fowler", "Martin"))
                .verifyComplete();
    }
}
