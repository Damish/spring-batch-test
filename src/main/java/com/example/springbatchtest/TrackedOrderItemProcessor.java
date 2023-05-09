package com.example.springbatchtest;

import org.springframework.batch.item.ItemProcessor;

import java.util.UUID;

public class TrackedOrderItemProcessor implements ItemProcessor<Order, TrackedOrder> {
    @Override
    public TrackedOrder process(Order item) throws Exception {
        System.out.println("processing order with id: "+item.getOrderId());
        TrackedOrder trackedOrder = new TrackedOrder(item);
        trackedOrder.setTrackingNumber(this.getTrackingNumber());
        return trackedOrder;
    }

    private String getTrackingNumber() throws OrderProcessingException{
        if(Math.random() < .05){ // or 0.50 for job fail with exception
            throw new OrderProcessingException();
        }
        return UUID.randomUUID().toString();
    }

}
