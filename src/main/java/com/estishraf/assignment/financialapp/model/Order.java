package com.estishraf.assignment.financialapp.model;

import com.estishraf.assignment.financialapp.enums.OrderStatus;
import com.estishraf.assignment.financialapp.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Order {
    public UUID id;
    public Quote stock;
    public int quantity;
    public OrderType orderType;
    public OrderStatus orderStatus;
}