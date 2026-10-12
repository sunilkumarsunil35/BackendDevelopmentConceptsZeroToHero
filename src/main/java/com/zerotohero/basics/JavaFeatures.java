package com.zerotohero.basics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Module 01: the Java you use every day as a backend developer. Each method is small on purpose;
 * read it, then read its test in {@code JavaFeaturesTest}.
 */
public final class JavaFeatures {

    private JavaFeatures() {
    }

    /** A record is an immutable data carrier: constructor, accessors, equals, hashCode and toString for free. */
    public record Product(String name, String category, BigDecimal price) {
        public Product {
            // A compact constructor validates input once, so every Product is valid.
            if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
            if (price.signum() < 0) throw new IllegalArgumentException("price must not be negative");
        }
    }

    /** Streams: group products by category and sum the prices in each group. */
    public static Map<String, BigDecimal> totalByCategory(List<Product> products) {
        return products.stream()
                .collect(Collectors.groupingBy(Product::category,
                        Collectors.reducing(BigDecimal.ZERO, Product::price, BigDecimal::add)));
    }

    /** Streams: the names of the n most expensive products, most expensive first. */
    public static List<String> mostExpensive(List<Product> products, int n) {
        return products.stream()
                .sorted(Comparator.comparing(Product::price).reversed())
                .limit(n)
                .map(Product::name)
                .toList();
    }

    /** Optional: say "there may be no result" in the type instead of returning null. */
    public static Optional<Product> findByName(List<Product> products, String name) {
        return products.stream().filter(p -> p.name().equalsIgnoreCase(name)).findFirst();
    }

    /** Optional chaining: a price with tax, or a default when the product is missing. */
    public static BigDecimal priceWithTax(List<Product> products, String name, BigDecimal taxRate) {
        return findByName(products, name)
                .map(Product::price)
                .map(p -> p.multiply(BigDecimal.ONE.add(taxRate)).setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
    }

    /** Sealed types: the compiler knows every possible payment, so the switch below needs no default. */
    public sealed interface Payment permits Card, Upi, Cash {
    }

    public record Card(String last4, BigDecimal amount) implements Payment {
    }

    public record Upi(String vpa, BigDecimal amount) implements Payment {
    }

    public record Cash(BigDecimal amount) implements Payment {
    }

    /** Pattern matching for switch (Java 21) with record deconstruction and a guard. */
    public static String describe(Payment payment) {
        return switch (payment) {
            case Card(String last4, BigDecimal amount) -> "Card ending " + last4 + " charged " + amount;
            case Upi(String vpa, BigDecimal amount) when amount.compareTo(new BigDecimal("100000")) > 0 ->
                    "UPI payment to " + vpa + " needs extra verification";
            case Upi(String vpa, BigDecimal amount) -> "UPI payment of " + amount + " to " + vpa;
            case Cash c -> "Cash payment of " + c.amount();
        };
    }

    /** Higher-order functions: build a function from other functions. */
    public static Function<BigDecimal, BigDecimal> discountThenTax(BigDecimal discount, BigDecimal tax) {
        Function<BigDecimal, BigDecimal> applyDiscount = p -> p.subtract(p.multiply(discount));
        Function<BigDecimal, BigDecimal> applyTax = p -> p.add(p.multiply(tax));
        return applyDiscount.andThen(applyTax).andThen(p -> p.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Virtual threads (Java 21): run many blocking tasks concurrently without a big thread pool.
     * Each task sleeps to simulate an I/O call; 1,000 of them finish in about the time of one.
     */
    public static int runBlockingTasks(int tasks, Duration eachTakes) throws InterruptedException, ExecutionException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<Integer>> work = java.util.stream.IntStream.range(0, tasks)
                    .<Callable<Integer>>mapToObj(i -> () -> {
                        Thread.sleep(eachTakes);
                        return 1;
                    })
                    .toList();
            int done = 0;
            for (Future<Integer> f : executor.invokeAll(work)) done += f.get();
            return done;
        }
    }

    /** Checked vs unchecked: wrap a low-level checked exception in a meaningful unchecked one. */
    public static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value.trim());
            if (port < 1 || port > 65_535) throw new IllegalArgumentException("port out of range: " + port);
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("not a number: " + value, e);
        }
    }
}
