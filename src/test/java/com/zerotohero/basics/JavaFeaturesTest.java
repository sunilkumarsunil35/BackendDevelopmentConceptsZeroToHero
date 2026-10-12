package com.zerotohero.basics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zerotohero.basics.JavaFeatures.Card;
import com.zerotohero.basics.JavaFeatures.Cash;
import com.zerotohero.basics.JavaFeatures.Product;
import com.zerotohero.basics.JavaFeatures.Upi;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Module 01: plain JUnit 5 tests, no Spring needed. */
class JavaFeaturesTest {

    private final List<Product> products = List.of(
            new Product("Keyboard", "electronics", new BigDecimal("40.00")),
            new Product("Mouse", "electronics", new BigDecimal("20.00")),
            new Product("Desk", "furniture", new BigDecimal("150.00")),
            new Product("Lamp", "furniture", new BigDecimal("35.50")));

    @Test
    void recordsValidateInTheCompactConstructor() {
        assertThatThrownBy(() -> new Product(" ", "x", BigDecimal.ONE)).isInstanceOf(IllegalArgumentException.class);
        assertThat(new Product("A", "c", BigDecimal.ONE)).isEqualTo(new Product("A", "c", BigDecimal.ONE));
    }

    @Test
    void groupsAndSumsWithStreams() {
        assertThat(JavaFeatures.totalByCategory(products))
                .containsEntry("electronics", new BigDecimal("60.00"))
                .containsEntry("furniture", new BigDecimal("185.50"));
    }

    @Test
    void sortsAndLimits() {
        assertThat(JavaFeatures.mostExpensive(products, 2)).containsExactly("Desk", "Keyboard");
    }

    @Test
    void optionalInsteadOfNull() {
        assertThat(JavaFeatures.findByName(products, "lamp")).isPresent();
        assertThat(JavaFeatures.findByName(products, "chair")).isEmpty();
        assertThat(JavaFeatures.priceWithTax(products, "Mouse", new BigDecimal("0.18"))).isEqualByComparingTo("23.60");
        assertThat(JavaFeatures.priceWithTax(products, "chair", new BigDecimal("0.18"))).isEqualByComparingTo("0");
    }

    @Test
    void patternMatchingOverSealedTypes() {
        assertThat(JavaFeatures.describe(new Card("4242", BigDecimal.TEN))).isEqualTo("Card ending 4242 charged 10");
        assertThat(JavaFeatures.describe(new Upi("asha@upi", new BigDecimal("500")))).startsWith("UPI payment of 500");
        assertThat(JavaFeatures.describe(new Upi("asha@upi", new BigDecimal("200000")))).contains("extra verification");
        assertThat(JavaFeatures.describe(new Cash(BigDecimal.ONE))).isEqualTo("Cash payment of 1");
    }

    @Test
    void composesFunctions() {
        var pricing = JavaFeatures.discountThenTax(new BigDecimal("0.10"), new BigDecimal("0.05"));
        assertThat(pricing.apply(new BigDecimal("100"))).isEqualByComparingTo("94.50");
    }

    @Test
    void virtualThreadsRunThousandsOfBlockingTasksQuickly() throws Exception {
        long start = System.nanoTime();
        int done = JavaFeatures.runBlockingTasks(1_000, Duration.ofMillis(100));
        long ms = (System.nanoTime() - start) / 1_000_000;
        assertThat(done).isEqualTo(1_000);
        assertThat(ms).isLessThan(5_000); // 1,000 x 100 ms one after another would take 100 s
    }

    @Test
    void wrapsCheckedExceptions() {
        assertThat(JavaFeatures.parsePort(" 8080 ")).isEqualTo(8080);
        assertThatThrownBy(() -> JavaFeatures.parsePort("http"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasCauseInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> JavaFeatures.parsePort("70000")).hasMessageContaining("out of range");
    }
}
