package com.zerotohero.async;

import com.zerotohero.book.BookRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Module 15: a scheduled job. Cron fields are second, minute, hour, day of month, month, day of week;
 * this one runs at 09:00 every weekday. In a cluster, every instance would run it: use a lock such
 * as ShedLock (see the lesson) so only one does.
 */
@Component
public class LowStockReporter {

    private static final Logger log = LoggerFactory.getLogger(LowStockReporter.class);
    private final BookRepository books;
    private final int threshold;

    public LowStockReporter(BookRepository books, @Value("${app.low-stock.threshold:3}") int threshold) {
        this.books = books;
        this.threshold = threshold;
    }

    @Scheduled(cron = "${app.low-stock.cron:0 0 9 * * MON-FRI}")
    @Transactional(readOnly = true)
    public List<String> report() {
        List<String> titles = books.findLowStock(threshold).stream()
                .map(b -> b.getTitle() + " (" + b.getStock() + " left)")
                .toList();
        if (!titles.isEmpty()) log.warn("Low stock: {}", titles);
        return titles;
    }
}
