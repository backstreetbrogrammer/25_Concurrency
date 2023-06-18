package com.backstreetbrogrammer.ch02_orderingReadAndWrite.studentsLibrary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentsLibraryDemo {

    private static final int NUM_OF_STUDENTS = 5;
    private static final int NUM_OF_BOOKS = 7;

    public static void main(final String[] args) {
        final Book[] books = new Book[NUM_OF_BOOKS];
        for (int i = 0; i < NUM_OF_BOOKS; i++) {
            books[i] = new Book(i + 1);
        }

        final Student[] students = new Student[NUM_OF_STUDENTS];
        final ExecutorService executorService = Executors.newFixedThreadPool(NUM_OF_STUDENTS);

        try {
            for (int i = 0; i < NUM_OF_STUDENTS; i++) {
                students[i] = new Student(i + 1, books);
                executorService.execute(students[i]);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }
}
