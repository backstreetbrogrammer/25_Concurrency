package com.backstreetbrogrammer.ch02_orderingReadAndWrite.diningPhilosophers;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Philosopher implements Runnable {
    private final int id;
    private final Fork leftFork;
    private final Fork rightFork;

    private volatile boolean full;
    private int eatingCounter;

    public Philosopher(final int id, final Fork leftFork, final Fork rightFork) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    @Override
    public void run() {
        try {
            // after eating a lot for random 1000 ms (1 seconds), we will terminate the given thread
            while (!full) {
                think();
                if (leftFork.pickUp(this, State.LEFT)) {
                    if (rightFork.pickUp(this, State.RIGHT)) {
                        eat();
                        rightFork.putDown(this, State.RIGHT);
                    }
                    leftFork.putDown(this, State.LEFT);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void think() throws InterruptedException {
        System.out.printf("%s is thinking...%n", this);
        TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextLong(1000L));
    }

    private void eat() throws InterruptedException {
        System.out.printf("%s is eating...%n", this);
        eatingCounter++;
        TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextLong(1000L));
    }

    public void setFull(final boolean full) {
        this.full = full;
    }

    public boolean isFull() {
        return full;
    }

    public int getEatingCounter() {
        return eatingCounter;
    }

    @Override
    public String toString() {
        return String.format("Philosopher %d", id);
    }
}
