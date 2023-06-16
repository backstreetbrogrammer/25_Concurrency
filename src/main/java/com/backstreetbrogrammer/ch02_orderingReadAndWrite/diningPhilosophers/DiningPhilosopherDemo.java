package com.backstreetbrogrammer.ch02_orderingReadAndWrite.diningPhilosophers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DiningPhilosopherDemo {

    private static final int NUMBER_OF_PHILOSOPHERS = 5;
    private static final int NUMBER_OF_FORKS = 5;
    private static final int SIMULATION_RUNNING_TIME = 5 * 1000;

    public static void main(final String[] args) throws InterruptedException {
        final Philosopher[] philosophers = new Philosopher[NUMBER_OF_PHILOSOPHERS];

        final Fork[] forks = new Fork[NUMBER_OF_FORKS];
        for (int i = 0; i < forks.length; i++) {
            forks[i] = new Fork(i);
        }

        final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_PHILOSOPHERS);

        try {
            for (int i = 0; i < philosophers.length; i++) {
                philosophers[i] = new Philosopher(i, forks[i], forks[(i + 1) % forks.length]);
                executorService.execute(philosophers[i]);
            }

            TimeUnit.MILLISECONDS.sleep(SIMULATION_RUNNING_TIME);

            for (final Philosopher philosopher : philosophers) {
                philosopher.setFull(true);
            }
        } finally {
            executorService.shutdown();

            while (!executorService.isTerminated()) {
                TimeUnit.MILLISECONDS.sleep(1000L);
            }

            for (final Philosopher philosopher : philosophers) {
                System.out.printf("%s eat #%d times%n", philosopher, philosopher.getEatingCounter());
            }
        }
    }
}
