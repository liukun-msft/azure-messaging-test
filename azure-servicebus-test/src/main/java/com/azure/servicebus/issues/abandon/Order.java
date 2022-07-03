package com.azure.servicebus.issues.abandon;

public class Order {
    public Integer orderNo;

    public Order(String orderNo) {
        this.orderNo = Integer.parseInt(orderNo);
    }

    public Integer getOrderNo() {
        return this.orderNo;
    }
}
