package com.example.springbatchtest;

public class CustomSkipListener implements org.springframework.batch.core.SkipListener<Order, TrackedOrder> {


    @Override
    public void onSkipInRead(Throwable t) {

    }

    @Override
    public void onSkipInWrite(TrackedOrder item, Throwable t) {

    }

    @Override
    public void onSkipInProcess(Order item, Throwable t) {
        System.out.println("skipping processing the item with id: "+item.getOrderId());
    }
}
