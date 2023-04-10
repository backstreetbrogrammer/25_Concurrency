package com.backstreetbrogrammer.ch01_introduction;

public class CreateThreadDemo {

    public static void main(final String[] args) {
        final Runnable runnable = () -> System.out.printf("I am running in this thread: %s%n",
                                                          Thread.currentThread().getName());
        final Thread thread = new Thread(runnable, "MyThread");
        thread.start();
    }

}
