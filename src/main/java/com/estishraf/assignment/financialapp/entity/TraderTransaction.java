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
}
