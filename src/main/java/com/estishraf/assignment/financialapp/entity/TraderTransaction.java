package com.estishraf.assignment.financialapp.entity;

import com.estishraf.assignment.financialapp.enums.OrderType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
public class TraderTransaction {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(targetEntity = Trader.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "trader", foreignKey = @ForeignKey(name = "FK_TraderTransaction_Trader"))
    private Trader trader;

    @Column
    private String stock;

    @Column
    private BigDecimal price;

    @Column
    private int quantity;

    @Column
    private OrderType type;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    public TraderTransaction() {
    }

    public TraderTransaction(UUID id, Trader trader, String stock, BigDecimal price, int quantity, OrderType type, Date time) {
        this.id = id;
        this.trader = trader;
        this.stock = stock;
        this.price = price;
        this.quantity = quantity;
        this.type = type;
        this.time = time;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Trader getTrader() {
        return trader;
    }

    public void setTrader(Trader trader) {
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

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
