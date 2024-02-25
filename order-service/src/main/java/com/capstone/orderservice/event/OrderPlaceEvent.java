package com.capstone.orderservice.event;

import lombok.*;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
public class OrderPlaceEvent extends ApplicationEvent {
    private String orderNumber;

    public OrderPlaceEvent(Object source, String orderNumber) {
        super(source);
        this.orderNumber = orderNumber;
    }

    public OrderPlaceEvent(String orderNumber) {
        super(orderNumber);
        this.orderNumber = orderNumber;
    }
}