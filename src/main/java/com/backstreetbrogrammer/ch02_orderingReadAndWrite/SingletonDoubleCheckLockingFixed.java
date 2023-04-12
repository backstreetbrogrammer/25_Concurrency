package com.backstreetbrogrammer.ch02_orderingReadAndWrite;

public class SingletonDoubleCheckLockingFixed {

    private static volatile SingletonDoubleCheckLockingFixed instance;
    private static final Object lock = new Object();

    private SingletonDoubleCheckLockingFixed() {
    }

    public static SingletonDoubleCheckLockingFixed getInstance() {
        if (instance != null) {                               // read operation - protected by volatile
            return instance;
        }

        synchronized (lock) {
            if (instance == null) {                                   // read operation - synchronized
                instance = new SingletonDoubleCheckLockingFixed();    // write operation - synchronized
            }
            return instance;
        }
    }

}
