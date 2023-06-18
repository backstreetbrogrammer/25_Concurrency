package com.backstreetbrogrammer.ch02_orderingReadAndWrite.studentsLibrary;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Book {

    private final int id;
    private final Lock lock;

    public Book(final int id) {
        this.id = id;
        this.lock = new ReentrantLock();
    }

    public void read(final Student student) throws InterruptedException {
        if (lock.tryLock(10, TimeUnit.MINUTES)) {
            try {
                System.out.printf("%s start reading %s%n", student, this);
                TimeUnit.SECONDS.sleep(2L);
            } finally {
                lock.unlock();
                System.out.printf("%s has just finished reading %s%n", student, this);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Book %d", id);
    }
}
