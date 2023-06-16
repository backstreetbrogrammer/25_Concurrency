package com.backstreetbrogrammer.ch02_orderingReadAndWrite.diningPhilosophers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Fork {

    private final int id;
    private final Lock lock;

    public Fork(final int id) {
        this.id = id;
        this.lock = new ReentrantLock();
    }

    public boolean pickUp(final Philosopher philosopher, final ForkPosition forkPosition) throws InterruptedException {
        if (lock.tryLock(10, TimeUnit.MILLISECONDS)) {
            System.out.printf("%s picked up %s %s%n", philosopher, forkPosition.toString(), this);
            return true;
        }
        return false;
    }

    public void putDown(final Philosopher philosopher, final ForkPosition forkPosition) {
        lock.unlock();
        System.out.printf("%s puts down %s %s%n", philosopher, forkPosition.toString(), this);
    }

    @Override
    public String toString() {
        return String.format("Fork %d", id);
    }
}
