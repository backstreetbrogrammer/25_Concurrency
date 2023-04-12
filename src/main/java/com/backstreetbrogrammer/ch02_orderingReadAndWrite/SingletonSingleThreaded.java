package com.backstreetbrogrammer.ch02_orderingReadAndWrite;

public class SingletonSingleThreaded {

    private static SingletonSingleThreaded instance;

    private SingletonSingleThreaded() {
    }

    public static SingletonSingleThreaded getInstance() {
        if (instance == null) {                        // read operation
            instance = new SingletonSingleThreaded();  // write operation
        }
        return instance;
    }

}
