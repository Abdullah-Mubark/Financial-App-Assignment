package com.estishraf.assignment.financialapp.entity;

import com.estishraf.assignment.financialapp.enums.OrderType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class TraderTransaction {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    @Column
    private String trader;
    @Column
    private String stock;
    @Column
    private BigDecimal price;
    @Column
    private int quantity;
    @Column
    private OrderType type;

    public TraderTransaction() {

    }

    public TraderTransaction(UUID id, String trader, String stock, BigDecimal price, int quantity, OrderType type) {
        this.id = id;
        this.trader = trader;
        this.stock = stock;
        this.price = price;
        this.quantity = quantity;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTrader() {
        return trader;
    }

    public void setTrader(String trader) {
        this.trader = trader;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }
}
