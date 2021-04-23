package com.estishraf.assignment.financialapp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="TraderTransaction")
public class TraderTransaction {
    @Id
    @Column
    private UUID id;
    @Column
    private String trader;
    @Column
    private String stock;
    @Column
    private BigDecimal price;
    @Column
    private int quantity;

    @Id
    public UUID getId() {
        return id;
    }

    public String getTrader() {
        return trader;
    }

    public void setTrader(String trader) {
        this.trader = trader;
    }

    public void setId(UUID id) {
        this.id = id;
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
}
