package com.zerotohero.book;

import java.math.BigDecimal;

/** Module 09: an interface projection. Spring Data selects only these columns. */
public interface BookSummary {
    String getTitle();

    BigDecimal getPrice();
}
