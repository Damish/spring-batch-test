package com.example.springbatchtest;

import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class FreeShippingItemProcessor implements ItemProcessor<TrackedOrder, TrackedOrder> {
    @Override
    public TrackedOrder process(TrackedOrder item) throws Exception {

        if (item.getCost().compareTo(new BigDecimal("80")) == 1) {
            item.setFreeShipping(true);
        } else {
            item.setFreeShipping(false);
        }
        return item.isFreeShipping() ? item : null;
    }
}
