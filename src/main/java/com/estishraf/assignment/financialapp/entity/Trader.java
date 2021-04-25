package com.estishraf.assignment.financialapp.entity;

import com.estishraf.assignment.financialapp.enums.TraderStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
public class Trader {

    @Id
    private String name;

    @Column
    private BigDecimal balance;

    @Column
    private TraderStrategy strategy;

    public Trader() {
    }

    public Trader(String name, BigDecimal balance, TraderStrategy strategy) {
        this.name = name;
        this.balance = balance;
        this.strategy = strategy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public TraderStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(TraderStrategy strategy) {
        this.strategy = strategy;
    }
}
