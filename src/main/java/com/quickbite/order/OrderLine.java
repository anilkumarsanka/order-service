package com.quickbite.order;

import jakarta.persistence.*;

@Entity
public class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long itemId;
    public String name;
    public int qty;
    public double unitPrice;
}
