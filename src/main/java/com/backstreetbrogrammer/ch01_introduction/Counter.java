package com.backstreetbrogrammer.ch01_introduction;

public class Counter {

    private long counter;

    public Counter(final long counter) {
        this.counter = counter;
    }

    public synchronized long getCounter() { // read
        return counter;
    }

    public synchronized void increment() { // write
        counter += 1L;
    }

}
