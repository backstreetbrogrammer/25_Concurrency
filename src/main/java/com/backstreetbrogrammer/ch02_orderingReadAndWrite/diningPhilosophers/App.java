package com.backstreetbrogrammer.ch02_orderingReadAndWrite.diningPhilosophers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(final String[] args) throws InterruptedException {
        ExecutorService executorService = null;
        Philosopher[] philosophers = null;
        final Fork[] forks;

        try {
            philosophers = new Philosopher[Constants.NUMBER_OF_PHILOSOPHERS];
            forks = new Fork[Constants.NUMBER_OF_FORKS];

            for (int i = 0; i < forks.length; i++) {
                forks[i] = new Fork(i);
            }

            executorService = Executors.newFixedThreadPool(philosophers.length);

            for (int i = 0; i < philosophers.length; i++) {
                philosophers[i] = new Philosopher(i, forks[i], forks[(i + 1) % forks.length]);
                executorService.execute(philosophers[i]);
            }

            TimeUnit.MILLISECONDS.sleep(Constants.SIMULATION_RUNNING_TIME);

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
