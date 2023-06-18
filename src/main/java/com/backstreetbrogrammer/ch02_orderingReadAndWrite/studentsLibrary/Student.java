package com.backstreetbrogrammer.ch02_orderingReadAndWrite.studentsLibrary;

import java.util.concurrent.ThreadLocalRandom;

public class Student implements Runnable {

    private final int id;
    private final Book[] books;

    public Student(final int id, final Book[] books) {
        this.id = id;
        this.books = books;
    }

    @Override
    public void run() {
        while (true) {
            final int bookId = ThreadLocalRandom.current().nextInt(books.length);
            try {
                books[bookId].read(this);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Student %d", id);
    }
}
