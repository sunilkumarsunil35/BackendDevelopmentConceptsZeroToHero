package com.zerotohero.book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Module 09: a JPA entity. Module 10 adds {@code @Version} for optimistic locking, and
 * auditing fills createdAt/updatedAt automatically.
 */
@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    // LAZY: don't load the author until someone calls getAuthor(). Always prefer LAZY for @ManyToOne.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private Author author;

    // Optimistic locking: an UPDATE fails if someone else changed the row since we read it.
    @Version
    private long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    protected Book() {
    }

    public Book(String title, String isbn, BigDecimal price, int stock, Author author) {
        this.title = title;
        this.isbn = isbn;
        this.price = price;
        this.stock = stock;
        this.author = author;
    }

    /** Domain logic lives on the entity: stock can never go below zero. */
    public void removeStock(int quantity) {
        if (quantity > stock) {
            throw new InsufficientStockException(id, title, stock, quantity);
        }
        stock -= quantity;
    }

    public void update(String title, String isbn, BigDecimal price, int stock, Author author) {
        this.title = title;
        this.isbn = isbn;
        this.price = price;
        this.stock = stock;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public Author getAuthor() {
        return author;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
