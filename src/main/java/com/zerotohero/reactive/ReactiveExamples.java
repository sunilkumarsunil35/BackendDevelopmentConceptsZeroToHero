package com.zerotohero.reactive;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Module 22: Project Reactor, the library under Spring WebFlux. A {@link Mono} is 0 or 1 value
 * that arrives later; a {@link Flux} is 0 to N values. Nothing runs until someone subscribes.
 * Each method is tested with StepVerifier in ReactiveExamplesTest.
 */
public final class ReactiveExamples {

    private ReactiveExamples() {
    }

    /** map and filter work like streams, but on values that arrive over time. */
    public static Flux<String> expensiveTitles(Flux<Book> books, double minPrice) {
        return books.filter(b -> b.price() >= minPrice).map(Book::title).map(String::toUpperCase);
    }

    public record Book(String title, double price, String author) {
    }

    public record Review(String title, int stars) {
    }

    /** flatMap: for each book, call another async source (e.g. a reviews service) and merge results. */
    public static Flux<Review> reviewsFor(Flux<Book> books, Function<String, Flux<Review>> reviewService) {
        return books.flatMap(b -> reviewService.apply(b.title()));
    }

    /** zip: wait for two independent calls and combine them, like CompletableFuture.thenCombine. */
    public static Mono<String> bookWithRating(Mono<Book> book, Mono<Double> rating) {
        return Mono.zip(book, rating, (b, r) -> b.title() + " rated " + r);
    }

    /** Error handling: retry a flaky call, then fall back to a default value. */
    public static Mono<String> withFallback(Mono<String> flaky, int retries, String fallback) {
        return flaky.retryWhen(Retry.max(retries)).onErrorResume(e -> Mono.just(fallback));
    }

    /** Timeouts: give up on a slow source and use a fallback. */
    public static Mono<String> withTimeout(Mono<String> slow, Duration timeout, String fallback) {
        return slow.timeout(timeout).onErrorReturn(fallback);
    }

    /** Backpressure: the subscriber asks for items in batches of n instead of being flooded. */
    public static Flux<Integer> inBatches(Flux<Integer> source, int n) {
        return source.limitRate(n);
    }

    /** Collect a stream into one value when the caller needs a list. */
    public static Mono<List<String>> authors(Flux<Book> books) {
        return books.map(Book::author).distinct().sort().collectList();
    }
}
