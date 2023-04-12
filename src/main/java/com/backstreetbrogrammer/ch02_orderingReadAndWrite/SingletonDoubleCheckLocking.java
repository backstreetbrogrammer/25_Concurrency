package com.backstreetbrogrammer.ch02_orderingReadAndWrite;

public class SingletonDoubleCheckLocking {

    private static SingletonDoubleCheckLocking instance;
    private static final Object lock = new Object();

    private SingletonDoubleCheckLocking() {
    }

    public static SingletonDoubleCheckLocking getInstance() {
        if (instance != null) {                               // read operation - not synchronized
            return instance;
        }

        synchronized (lock) {
            if (instance == null) {                              // read operation - synchronized
                instance = new SingletonDoubleCheckLocking();    // write operation - synchronized
            }
            return instance;
        }
    }

}
