package com.backstreetbrogrammer.ch01_introduction;

public class Counter {

    private long counter;

    public Counter(final long counter) {
        this.counter = counter;
    }

    public long getCounter() {
        return counter;
    }

    public synchronized void increment() {
        counter += 1L;
    }

}
