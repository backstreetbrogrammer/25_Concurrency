package com.backstreetbrogrammer.ch02_orderingReadAndWrite;

public class SingletonSynchronized {

    private static SingletonSynchronized instance;

    private SingletonSynchronized() {
    }

    public static synchronized SingletonSynchronized getInstance() {
        if (instance == null) {                        // read operation
            instance = new SingletonSynchronized();    // write operation
        }
        return instance;
    }

}
