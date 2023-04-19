package com.example.springbatchtest;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleItemReader implements ItemReader<String> {

    public List<String> dataset = new ArrayList<>();

    public Iterator<String> iterator;

    public SimpleItemReader() {
        /* for (int i = 0; i < 50; i++) {
            this.dataset.add(String.valueOf(i));
        }*/
        this.dataset.add("1");
        this.dataset.add("2");
        this.dataset.add("3");
        this.dataset.add("4");
        this.dataset.add("5");
        this.iterator = this.dataset.iterator();
    }

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
