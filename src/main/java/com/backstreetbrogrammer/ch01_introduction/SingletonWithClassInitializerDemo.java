package com.backstreetbrogrammer.ch01_introduction;

public class SingletonWithClassInitializerDemo {

    // write once when the class is initialized - always thread safe
    private static SingletonWithClassInitializerDemo instance = new SingletonWithClassInitializerDemo();

    private SingletonWithClassInitializerDemo() {
    }

    public static SingletonWithClassInitializerDemo getInstance() {
        return instance; // no lock required for reads by multiple threads
    }

}
