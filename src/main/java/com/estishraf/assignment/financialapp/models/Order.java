package com.estishraf.assignment.financialapp.models;

import com.estishraf.assignment.financialapp.enums.OrderStatus;
import com.estishraf.assignment.financialapp.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Order {
    public Quote stock;
    public int quantity;
    public OrderType orderType;
    public OrderStatus orderStatus;
}