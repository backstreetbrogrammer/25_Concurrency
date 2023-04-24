# Concurrency

> This is a tutorials course covering concurrency in Java.

Tools used:

- JDK 11
- Maven
- JUnit 5, Mockito
- IntelliJ IDE

## Table of contents

### Module 1 - Concurrency Basics

1. Introduction to Concurrency
    - Threading fundamentals
    - Thread coordination
2. Ordering read and write operations
    - Synchronization and Visibility
    - Java Memory Model
    - False Sharing
    - Singleton design pattern

### Module 2 - Advanced Concurrency

3. Executor Pattern, Callable and Future
4. Fork/Join Framework
5. Advanced Locking and Semaphores
6. Using Barriers and Latches
7. CAS operation and Atomic classes
8. Concurrent Collections

---

## Module 1 - Concurrency Basics

### Chapter 01. Introduction to Concurrency

#### Threading fundamentals

In computer science, **concurrency** is the execution of the multiple instruction sequences at the **same** time.

In more technical terms, **concurrency** is the ability of different parts or units of a program, algorithm, or problem
to be executed out-of-order or in partial order, without affecting the outcome. This allows for **parallel** execution
of the concurrent units, which can significantly improve overall speed of the execution in multiprocessor and multicore
systems. It may also refer to the _decomposability_ of a program, algorithm, or problem into order-independent or
partially-ordered components or units of computation.

- Case 1: CPU with only one core

CPU will be executing processes one by one, individually by **time slice**. A time slice is short time frame that gets
assigned to process for CPU execution.

- Case 2: CPU with multiple cores

Only on multicore CPU system, multiple processes execute at the **same** time on different cores.

**Scheduler**

CPU Scheduling is a process that allows one process to use the CPU while another process is delayed (in standby) due to
unavailability of any resources such as I/O etc., thus making full use of the CPU. Whenever the CPU becomes idle, the
operating system must select one of the processes in the line ready for launch. The selection process is done by a
temporary (CPU) scheduler. The Scheduler selects between memory processes ready to launch and assigns the CPU to one of
them.

Scheduler may **pause** a thread due to:

- The thread is waiting for some more data
- The thread is waiting for another thread to do something
- CPU should be shared equally among threads

**What is a Java Thread?**

We can define threads as a light-weight **subprocess** within the smallest unit of **processes** and having separate
paths of execution. These threads use shared memory, but they act independently. Hence, if there is an exception in a
thread, that will not affect the working of other threads despite them sharing the same memory.

Few points about Java Thread:

- Thread is a set of instructions defined at Operating System level
- Lightweight sub-process through which we can perform multiple activities within a single process
- An application can be composed of several threads => JVM itself works with several threads like GC, JIT, etc.
- Different threads can be executed at the **same** time on different cores or cpus

**Race Condition**

By definition, a race condition is a condition of a program where its behavior depends on relative timing or
interleaving of multiple threads or processes.

In simpler words, it means that two different threads are trying to **read** and **write** the **same** variable at
the **same** time.

#### Interview Problem 1 (Macquarie, Merrill Lynch): Demonstrate race condition in Singleton pattern and how to fix it

Given source code for Singleton pattern:

```java
public class SingletonDemo {

    private static SingletonDemo instance;

    private SingletonDemo() {
    }

    public static SingletonDemo getInstance() {
        if (instance == null) {
            instance = new SingletonDemo();
        }
        return instance;
    }

}
```

Explanation for **race condition**:

Suppose there are 2 threads `T1` and `T2` calling `getInstance()` method at the same time.

- `T1` thread gets CPU time and reaches at point: `if (instance == null)`
- The thread scheduler pauses `T1`
- `T2` thread gets CPU time, and it passes this line of code and creates new instance: `instance = new SingletonDemo()`
- The thread scheduler pauses `T2`
- `T1` thread gets CPU time, and it resumes from `if (instance == null)` check: it will also create a new
  instance: `instance = new SingletonDemo()`; thus breaking the Singleton pattern contract.

**Solution**:

Use **synchronization**.

Synchronization prevents a block of code to be executed by more than one thread at the same time.

```java
public class SingletonDemo {

    private static SingletonDemo instance;

    private SingletonDemo() {
    }

    public static synchronized SingletonDemo getInstance() {
        if (instance == null) {              // read
            instance = new SingletonDemo();  // write
        }
        return instance;
    }

}
```

For synchronization to work, we need a synchronization object key also called as monitor or mutex - every Java object
can play as monitor or mutex.

In the `static` method context => Class object is synchronization object key

```
// SingletonDemo.class => synchronization object key
public static synchronized SingletonDemo getInstance() {
   ...
}
```

In the `non-static` method context => instance object is synchronization object key

```
// instance object which invokes this method is synchronization object key
public synchronized SingletonDemo getInstance() {
   ...
}
```

Or, we can also explicitly use a Java object as synchronization object key using **synchronized block**

```
// instance object used explicitly as synchronization object key in synchronized block
public SingletonDemo getInstance() {
   synchronized(this) {
     ...
   }
}
```

Another solution is that somehow we can segregate read / write logic so that one thread writes the object synchronously
and after that, all the subsequent reads do not require any locking or synchronization.

```java
public class SingletonDemo {

    // write once when the class is initialized - always thread safe 
    private static SingletonDemo instance = new SingletonDemo();

    private SingletonDemo() {
    }

    public static SingletonDemo getInstance() {
        return instance; // no lock required for reads by multiple threads
    }

}
```

When the class loader loads the `SingletonDemo` class, **instance** object is created at that time. Thus, further
subsequent calls to `getInstance()` method during application run will always be thread safe and don't require any lock.

**Reentrant Lock**

Java locks are **reentrant** => when a thread holds a lock, it can enter a block synchronized on the lock it is holding.

```
public synchronized SingletonDemo getInstance() {
   ...
}

public synchronized void doSomething() {
   ...
}
```

A thread `T1` which has acquired instance lock by entering the `getInstance()` method can also enter `doSomething()`
method as the instance lock is same for both the methods. Any other thread can **not** acquire the instance lock and
enter these 2 methods as it's held by thread `T1`.

**Deadlock**

A deadlock is a situation where a thread `T1` holds a key needed by a thread `T2`, and `T2` holds the key needed by
`T1`. In this situation, both the threads will keep on waiting for each other indefinitely.

There is nothing we can do if the deadlock situation happens but to restart the JVM. However, even identifying a
deadlock is very complex in the modern JVMs or monitoring tools.

**How to create and run threads in Java?**

The most basic way is to:

- create a `Runnable` instance
- pass it to the `Thread` constructor
- invoke `start()` method on `Thread` object

**Example source code**:

```java
public class CreateThreadDemo {

    public static void main(final String[] args) {
        final Runnable runnable = () -> System.out.printf("I am running in this thread: %s%n",
                                                          Thread.currentThread().getName());
        final Thread thread = new Thread(runnable, "MyThread");
        thread.start();
    }

}
```

**Output**:

```
I am running in this thread: MyThread
```

#### Interview Problem 2 (JP Morgan Chase): Demonstrate synchronization issue and fix the code

Given Java Code:

```java
public class Counter {

    private long counter;

    public Counter(final long counter) {
        this.counter = counter;
    }

    public long getCounter() {
        return counter;
    }

    public void increment() {
        counter += 1L;
    }

}
```

```java
public class RaceConditionDemo {

    public static void main(final String[] args) throws InterruptedException {
        final Counter counter = new Counter(0L);
        final Runnable r = () -> {
            for (int i = 0; i < 1_000; i++) {
                counter.increment();
            }
        };

        final Thread[] threads = new Thread[1_000];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(r);
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        System.out.printf("Counter Value = %d%n", counter.getCounter());
    }

}
```

- What is the output of the counter value?
- Is the output going to be consistent for every run? If not, what is the issue?
- If any race condition, fix the code

**Solution**:

There is a race condition at `counter.increment()` as 1000 threads are trying to mutate the same variable `counter`
at the **same** time.

The output of the value will be different for each run.

Sample Outputs on 4 runs:

```
Counter Value = 994678
Counter Value = 994715
Counter Value = 995232
Counter Value = 980564
```

We need to synchronize the `increment()` method or synchronize the access to `counter` variable.

```
    public synchronized void increment() {
        counter += 1L;
    }
```

After synchronizing the `increment()` method, sample output on 4 runs:

```
Counter Value = 1000000
Counter Value = 1000000
Counter Value = 1000000
Counter Value = 1000000
```

However, still there is a subtle bug in the solution, and it **may** fail in multicore multithreaded environment.
Besides, synchronizing the `increment()` method, we also need to synchronize the `getCounter()` method. Because we need
to guarantee that every **read** is synchronized with latest **writes**, i.e. if any **read** of `counter` variable is
done - it should always happen after the latest **write** by a thread on the `counter` variable. This is called
**"happens-before"** relationship which we will learn later in next chapters.

Complete correct solution:

```java
public class Counter {

    private long counter;

    public Counter(final long counter) {
        this.counter = counter;
    }

    public synchronized long getCounter() { // read
        return counter;
    }

    public synchronized void increment() { // write
        counter += 1L;
    }

}
```

Also in advanced section of this course, we will learn to avoid explicit synchronization and just **atomic** classes to
achieve thread safety. For ex, in the above solution code - if we just make `counter` variable as `AtomicLong`, we do
not need to mark `getCounter()` and `increment()` as synchronized and can use special methods of `AtomicLong` to achieve
thread safety.

#### Interview Problem 3 (Goldman Sachs): Demonstrate deadlock issue and fix the code

Write a program to demonstrate deadlock issue where a thread `T1` holds a key needed by a thread `T2`, and `T2` holds
the key needed by `T1`. Fix the code.

Source code demonstrating deadlock issue:

```java
import java.util.concurrent.TimeUnit;

public class DeadlockDemo {

    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static void main(final String[] args) {
        new Thread1().start();
        new Thread2().start();
    }

    private static class Thread1 extends Thread {
        public void run() {
            synchronized (lock1) {
                System.out.println("Thread 1: Has lock1");
                try {
                    TimeUnit.MILLISECONDS.sleep(100L);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Thread 1: Waiting for lock2");
                synchronized (lock2) {
                    System.out.println("Thread 1: Has lock1 and lock2");
                }
                System.out.println("Thread 1: Released lock2");
            }
            System.out.println("Thread 1: Released lock1. Exiting...");
        }
    }

    private static class Thread2 extends Thread {
        public void run() {
            synchronized (lock2) {
                System.out.println("Thread 2: Has lock2");
                try {
                    TimeUnit.MILLISECONDS.sleep(100L);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread 2: Waiting for lock1");
                synchronized (lock1) {
                    System.out.println("Thread 2: Has lock1 and lock2");
                }
                System.out.println("Thread 2: Released lock1");
            }
            System.out.println("Thread 2: Released lock2. Exiting...");
        }
    }

}
```

`Thread1` acquires `lock1` and `Thread2` acquires `lock2`. Now both threads are waiting for other lock held by different
thread causing deadlock.

Output is stuck and application keeps on running (does not finish) with deadlock between 2 threads:

```
Thread 1: Has lock1
Thread 2: Has lock2
Thread 2: Waiting for lock1
Thread 1: Waiting for lock2
```

**Solution**:

Deadlock issue can be fixed by maintaining the same sequence for locks acquisition.

Both `Thread1` and `Thread2` can acquire `lock1` and `lock2` in the **same** sequence thus avoiding the deadlock issue.

```java
import java.util.concurrent.TimeUnit;

public class DeadlockDemo {

    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static void main(final String[] args) {
        new Thread1().start();
        new Thread2().start();
    }

    private static class Thread1 extends Thread {
        public void run() {
            synchronized (lock1) {
                System.out.println("Thread 1: Has lock1");
                try {
                    TimeUnit.MILLISECONDS.sleep(100L);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Thread 1: Waiting for lock2");
                synchronized (lock2) {
                    System.out.println("Thread 1: Has lock1 and lock2");
                }
                System.out.println("Thread 1: Released lock2");
            }
            System.out.println("Thread 1: Released lock1. Exiting...");
        }
    }

    private static class Thread2 extends Thread {
        public void run() {
            synchronized (lock1) {
                System.out.println("Thread 2: Has lock1");
                try {
                    TimeUnit.MILLISECONDS.sleep(100L);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread 2: Waiting for lock2");
                synchronized (lock2) {
                    System.out.println("Thread 2: Has lock1 and lock2");
                }
                System.out.println("Thread 2: Released lock2");
            }
            System.out.println("Thread 2: Released lock1. Exiting...");
        }
    }

}
```

Output is correct now with no deadlock:

```
Thread 1: Has lock1
Thread 1: Waiting for lock2
Thread 1: Has lock1 and lock2
Thread 1: Released lock2
Thread 1: Released lock1. Exiting...
Thread 2: Has lock1
Thread 2: Waiting for lock2
Thread 2: Has lock1 and lock2
Thread 2: Released lock2
Thread 2: Released lock1. Exiting...
```

#### Thread coordination

#### Interview Problem 4 (Barclays): How to stop a thread in Java?

We should NOT use `Thread.stop()` method as it is deprecated. `Thread.stop()` can lead to monitored objects being
corrupted, and it is inherently unsafe.

- Use a thread-safe `boolean` variable to control thread execution

Code snippet:

```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class StopThreadUsingBooleanDemo implements Runnable {
    private final AtomicBoolean running = new AtomicBoolean(false); // can also use 'volatile'

    @Override
    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                TimeUnit.MILLISECONDS.sleep(1L);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            // do the thread task
        }
    }

    public void stop() {
        running.set(false);
    }

}
```

Now once the thread is created and started - we can call `stop()` method to stop the thread.

```
final var stopThreadDemoObject = new StopThreadUsingBooleanDemo();
final Thread t1 = new Thread(stopThreadDemoObject);
t1.start();
...
stopThreadDemoObject.stop(); // this will stop the thread t1
```

- Call `interrupt()` on a running thread

It's very similar to the above boolean variable method.

Code Snippet:

```java
import java.util.concurrent.TimeUnit;

public class StopThreadUsingInterruptDemo implements Runnable {
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TimeUnit.MILLISECONDS.sleep(1L);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            // do the thread task
        }
    }

}
```

Now once the thread is created and started - we can call `interrupt()` method to stop the thread. The call to
`interrupt()` method will cause the `isInterrupted()` method to return `true`.

```
final Thread t1 = new Thread(new StopThreadUsingInterruptDemo());
t1.start();
...
t1.interrupt(); // this will cause the isInterrupted() method to return true
```

All the blocking methods like `wait(), notify(), notifyAll(), join(), sleep()` etc. throw `InterruptedException` based
on the same `interrupted` status of thread.

The interrupt mechanism is implemented using an internal flag known as the **interrupt status**.

- Invoking **non-static** `Thread.interrupt()` sets this flag
- When a thread checks for an interrupt by invoking the **static** method `Thread.interrupted()`, interrupt status is
  cleared
- The **non-static** `Thread.isInterrupted()` method, which is used by one thread to query the interrupt status of
  another, does NOT change the interrupt status flag

By convention, any method that exits by throwing an `InterruptedException` clears interrupt status when it does so.
However, it's always possible that interrupt status will immediately be set again, by another thread invoking interrupt.

#### Interview Problem 5 (Macquarie): Explain and Implement Producer Consumer pattern

We have a buffer - it can be an array, list, set or queue. A producer produces values in a buffer. A consumer consumes
the values from this buffer. Producers and Consumers are run in their own threads or thread pools.

Buffer can be **bounded** (having a defined capacity) or **unbounded** (based on system memory available).

Edge cases: the buffer can be full or empty => if it's full (bounded buffer) -> producers cannot write to it and if its
empty, consumers can not read from it.

![Producer Consumer](ProducerConsumer.PNG)

- Implementation 1 - source code:

**Producer**

```java
public class ProducerDemo1<T> {
    private final T[] buffer;
    private int count = 0;

    public ProducerDemo1(final T[] buffer) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException();
        }
        this.buffer = buffer;
    }

    public void produce(final T item) {
        while (isFull(buffer)) {
            // wait
        }
        buffer[count++] = item;
    }

    private boolean isFull(final T[] buffer) {
        return count == buffer.length;
    }

}
```

**Consumer**

```java
public class ConsumerDemo1<T> {
    private final T[] buffer;
    private int count = 0;

    public ConsumerDemo1(final T[] buffer) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException();
        }
        this.buffer = buffer;
    }

    public T consume() {
        while (isEmpty()) {
            // wait
        }
        return buffer[--count];
    }

    private boolean isEmpty() {
        return count == 0;
    }

}
```

Major flaw in this code: as several threads are producing (writing) and consuming (popping) the buffer at the same time
=> this will result in race condition. In other words, `buffer` and `count` variables are NOT thread-safe.

- Implementation 2 - use **synchronization**:

```
    public synchronized void produce(final T item) {
        while (isFull(buffer)) {
            // wait
        }
        buffer[count++] = item;
    }
```

```
    public synchronized T consume() {
        while (isEmpty()) {
            // wait
        }
        return buffer[--count];
    }
```

Using **synchronization** will help to fix the race condition problem => however, it will only synchronize all producer
threads to call `produce()` and synchronize all consumer threads to call `consume()`. **Producer** and **Consumer**
threads are still independent of each other and not synchronized across for both `produce()` and `consume()` methods. We
want the **common** buffer to be thread safe for both `produce()` and `consume()` methods.

- Implementation 3 - use **global lock** to be used by both Producer and Consumer:

**Producer**

```java
public class ProducerDemo3<T> {
    private final T[] buffer;
    private final Object lock;
    private int count = 0;

    public ProducerDemo3(final T[] buffer, final Object lock) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException();
        }
        this.buffer = buffer;
        this.lock = lock;
    }

    public void produce(final T item) {
        synchronized (lock) {
            while (isFull(buffer)) {
                // wait
            }
            buffer[count++] = item;
        }
    }

    private boolean isFull(final T[] buffer) {
        return count == buffer.length;
    }

}
```

**Consumer**

```java
public class ConsumerDemo3<T> {
    private final T[] buffer;
    private final Object lock;
    private int count = 0;

    public ConsumerDemo3(final T[] buffer, final Object lock) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException();
        }
        this.buffer = buffer;
        this.lock = lock;
    }

    public T consume() {
        synchronized (lock) {
            while (isEmpty()) {
                // wait
            }
            return buffer[--count];
        }
    }

    private boolean isEmpty() {
        return count == 0;
    }

}
```

Now both the `buffer` and Object `lock` are common to be used by both `Producer` and `Consumer`.

However, still this design has a major flaw!

Suppose if the buffer is empty => consumer thread will hold the lock object and keep on doing busy spinning inside:
`while (isEmpty(buffer))`. Producer threads will keep on waiting for this lock object held by the consumer thread
indefinitely and never be able to produce or write anything to buffer.

**Solution**:

We need a mechanism to somehow **"park"** this consumer thread when the buffer is empty and release the lock. Then the
producer thread can acquire this lock and write to the buffer. When the **"parked"** consumer thread is woken up again -
the buffer will not be empty this time, and it can consume the item.

This is the `wait()` / `notify()` pattern.

The `wait()`, `notify()` and `notifyAll()` methods are defined in `java.lang.Object` class. These methods are invoked on
a given object => normally the object lock being used. The thread executing the invocation should hold that object key.
Thus, in other words, these methods cannot be invoked outside a synchronized block.

Calling `wait()` releases the key (object lock) held by this thread and puts the thread in **WAIT** state. The only way
to release a thread from a **WAIT** state is to **notify** it.

Calling `notify()` release a thread in **WAIT** state and puts it in **RUNNABLE** state. This is the only way to release
a waiting thread. The released thread is chosen randomly. For `notifyAll()`, **all** the threads are moved from **WAIT**
state to **RUNNABLE** state, however only one thread can acquire the lock again. However, the woken threads can do other
task rather than waiting for the object again.

**Producer**:

```java
public class ProducerDemo4<T> {
    private final T[] buffer;
    private final Object lock;
    private int count = 0;

    public ProducerDemo4(final T[] buffer, final Object lock) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException();
        }
        this.buffer = buffer;
        this.lock = lock;
    }

    public void produce(final T item) throws InterruptedException {
        synchronized (lock) {
            try {
                while (isFull(buffer)) {
                    lock.wait();
                }
                buffer[count++] = item;
            } finally {
                lock.notifyAll();
            }
        }
    }

    private boolean isFull(final T[] buffer) {
        return count == buffer.length;
    }

}
```

**Consumer**:

```java
public class ConsumerDemo4<T> {
    private final T[] buffer;
    private final Object lock;
    private int count = 0;

    public ConsumerDemo4(final T[] buffer, final Object lock) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException();
        }
        this.buffer = buffer;
        this.lock = lock;
    }

    public T consume() throws InterruptedException {
        synchronized (lock) {
            try {
                while (isEmpty()) {
                    lock.wait();
                }
                return buffer[--count];
            } finally {
                lock.notifyAll();
            }
        }
    }

    private boolean isEmpty() {
        return count == 0;
    }

}
```

**Complete running example of Producer-Consumer Pattern**:

```java
import java.util.concurrent.TimeUnit;

public class ProducerConsumerMain {

    private static final Object lock = new Object();

    private static int[] buffer;
    private static int count;

    private static class Producer {
        void produce() {
            synchronized (lock) {
                while (isFull(buffer)) {
                    try {
                        lock.wait();
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                buffer[count++] = 1;
                lock.notifyAll();
            }
        }
    }

    private static class Consumer {
        void consume() {
            synchronized (lock) {
                while (isEmpty()) {
                    try {
                        lock.wait();
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                buffer[--count] = 0;
                lock.notifyAll();
            }
        }
    }

    private static boolean isEmpty() {
        return count == 0;
    }

    private static boolean isFull(final int[] buffer) {
        return count == buffer.length;
    }

    private static Runnable createProducerTask(final Producer producer, final int num, final String name) {
        return () -> {
            for (int i = 0; i < num; i++) {
                producer.produce();
            }
            System.out.printf("Done producing: %s%n", name);
        };
    }

    private static Runnable createConsumerTask(final Consumer consumer, final int num, final String name) {
        return () -> {
            for (int i = 0; i < num; i++) {
                consumer.consume();
            }
            System.out.printf("Done consuming: %s%n", name);
        };
    }

    public static void main(final String... strings) throws InterruptedException {
        buffer = new int[10];
        count = 0;

        final Thread[] producerThreads = new Thread[]{
                new Thread(createProducerTask(new Producer(), 30, "Producer1")),
                new Thread(createProducerTask(new Producer(), 20, "Producer2"))
        };
        final Thread[] consumerThreads = new Thread[]{
                new Thread(createConsumerTask(new Consumer(), 20, "Consumer1")),
                new Thread(createConsumerTask(new Consumer(), 15, "Consumer2")),
                new Thread(createConsumerTask(new Consumer(), 10, "Consumer3"))
        };

        for (final Thread producer : producerThreads) {
            producer.start();
        }
        for (final Thread consumer : consumerThreads) {
            consumer.start();
        }

        TimeUnit.SECONDS.sleep(1L);

        for (final Thread consumer : consumerThreads) {
            consumer.join();
        }
        for (final Thread producer : producerThreads) {
            producer.join();
        }

        System.out.printf("Data in the buffer: %d%n", count);
    }

}
```

Output:

```
Done consuming: Consumer1
Done producing: Producer2
Done consuming: Consumer3
Done consuming: Consumer2
Done producing: Producer1
Data in the buffer: 5
```

Here we have used the classic `wait()`/`notify()` methods but in the subsequent chapters, we will see how to use
advanced locking, condition variables or semaphores to get the same result.

#### Thread states

A thread has a state - for example, it can be running or not. We can get the thread state by calling `getState()`
method on thread.

```
final Thread t1 = new Thread();
...
Thread.State state = t1.getState();
```

Java API already defines **enum** `Thread.State` as
follows:  `public static enum Thread.State extends Enum<Thread. State>`

A thread can be in one of the following states:

- **NEW**: A thread that has not yet started is in this state.
- **RUNNABLE**: A thread executing in the Java virtual machine is in this state.
- **BLOCKED**: A thread that is blocked waiting for a monitor lock is in this state.
- **WAITING**: A thread that is waiting indefinitely for another thread to perform a particular action is in this state.
- **TIMED_WAITING**: A thread that is waiting for another thread to perform an action for up to a specified waiting time
  is in this state.
- **TERMINATED**: A thread that has exited is in this state.

![Thread States](ThreadStates.PNG)

A thread can be in only one state at a given point in time. These states are virtual machine states which do not reflect
any operating system thread states.

NOTE: If a thread is not running, can it be given hand by the thread scheduler ?

Answer is **no** => thread scheduler will only schedule threads which are in **RUNNABLE** state.

---

### Chapter 02. Ordering read and write operations

#### Synchronization and Visibility

**Synchronization** in Java is the capability to control the access of multiple threads to any shared resource. It
guarantees that a particular block of code or method is executed by ONLY one thread at a time. Thus, it prevents race
condition.

In the modern CPU architecture, CPU does not read a variable from the main memory but from a cache.

![CPU and caches](CPUCaches.PNG)

It has been designed as such to provide maximum performance:

- access to the main memory (RAM) =~ 100 ns
- access to the L2 cache =~ 7 ns
- access to the L1 cache =~ 0.5 ns

However, the data which can be stored is much less in caches than main memory. For ex: RAM can be in several GBs but L2
cache will be typically 256 KB and L1 cache will be around 32 KB only. So, only with very efficient caching strategy -
high performance can be achieved.

**Visibility**

A variable is said visible if the writes made on it by some thread is visible to all the other threads.

If two or more threads are sharing an object, without the proper use of either `volatile` declarations or
`synchronization`, updates to the shared object made by one thread may not be visible to other threads.

Imagine that the shared object is initially stored in main memory. A thread running on CPU one then reads the shared
object into its CPU cache. There it makes a change to the shared object. As long as the CPU cache has not been flushed
back to main memory, the changed version of the shared object is not visible to threads running on other CPUs. This way
each thread may end up with its own copy of the shared object, each copy sitting in a different CPU cache.

To solve this problem we can use Java's **volatile** keyword or wrap the variable inside **synchronized** block. The
**volatile** keyword can make sure that a given variable is read directly from main memory, and always written back to
main memory when updated.

Similarly, all the **synchronized writes** are **visible**.

#### Java Memory Model

According to Java Memory Model specs:

> A program must be correctly synchronized to avoid reordering and visibility problems.

A program is correctly synchronized if:

- Actions are ordered by **happens-before** relationship.
- Has no data races. Data races can be avoided by using **Intrinsic Locks**.

Multicore CPU brings new problems: Read and writes can really happen at the same time. A given variable can be stored in
more than one place. Visibility means "a read should return the value set by the **last** write".

Thus, we need a **timeline** to put read and write operations.

For ex:

1. A thread **T1** writes 1 to **x**: `x = 1;`
2. Another thread **T2** reads **x** and copy it to **y**: `y = x;`
3. What is the value of **y**?

If there is no **happens-before** relationship between the first two operations, the value of **y** is **unknown**.

If there is a **happens-before** relationship between the first two operations, the value of **y** is **1**. In other
words, **operation 1 happens before operation 2**.

A **"happens-before"** link exists between all **synchronized** or **volatile** _write_ operations and all
**synchronized** or **volatile** _read_ operations that follow.

Example code:

```
int counter;

void increment() {
    counter = counter + 1;
}

void print() {
    System.out.println(counter);
}
```

There are 2 operations here:

- a write operation - `increment()`
- a read operation - `print()`

In multithreaded code, we don't know what the value of `counter` will be.

If we use **synchronized** block here, **"happens-before"**  relationship is established and `counter` variable is
**visible**.

```
int counter;

void synchronized increment() {
    counter++;
}

void synchronized print() {
    System.out.println(counter);
}
```

Similarly, if we use **volatile** keyword, **"happens-before"**  relationship is established and `counter` variable is
**visible**.

```
volatile int counter;

void increment() {
    counter = counter + 1;
}

void print() {
    System.out.println(counter);
}
```

#### False Sharing

In computer science, false sharing is a performance-degrading usage pattern that can arise in systems with distributed,
coherent caches at the size of the smallest resource block managed by the caching mechanism.

False sharing in Java occurs when two threads running on two different CPUs write to two different variables which
happen to be stored within the same CPU cache line. When the first thread modifies one of the variables - the whole CPU
cache line is invalidated in the CPU caches of the other CPU where the other thread is running. This means, that the
other CPUs need to reload the content of the invalidated cache line - even if they don't really need the variable that
was modified within that cache line.

In simpler words, CPU cache is organized in lines of data. Each line can hold 8 longs - 64 bytes. When a visible
variable is modified in L1 or L2 cache, all the line is marked "dirty" for the other caches. A read on a dirty line
triggers a refresh of this line -> which is to flush the data from main memory.

![False Sharing](FalseSharing.PNG)

#### Interview Problem 6 (Standard Chartered Bank): Demonstrate false sharing in Java

Write a program to demonstrate false sharing in Java.

**Follow up**

Modify the program to resolve false sharing and improve performance. (Measure the performance)

Solution code:

```java
public class FalseSharingDemo {

    public final static class VolatileLongPadded {
        public long q1, q2, q3, q4, q5, q6;
        public volatile long value = 0L;
        public long q11, q12, q13, q14, q15, q16;

    }

    public final static class VolatileLongUnPadded {
        public volatile long value = 0L;
    }

}
```

Unit Test:

```java
import com.backstreetbrogrammer.ch02_orderingReadAndWrite.FalseSharingDemo.VolatileLongPadded;
import com.backstreetbrogrammer.ch02_orderingReadAndWrite.FalseSharingDemo.VolatileLongUnPadded;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

public class FalseSharingDemoTest {

    private VolatileLongPadded[] paddedLongs;
    private VolatileLongUnPadded[] unPaddedLongs;

    private final int numOfThreads = 4;
    private final long iterations = 100_000_000L;

    @BeforeEach
    void setUp() {
        paddedLongs = new VolatileLongPadded[]{
                new VolatileLongPadded(),
                new VolatileLongPadded(),
                new VolatileLongPadded(),
                new VolatileLongPadded()
        };

        unPaddedLongs = new VolatileLongUnPadded[]{
                new VolatileLongUnPadded(),
                new VolatileLongUnPadded(),
                new VolatileLongUnPadded(),
                new VolatileLongUnPadded()
        };
    }

    @Test
    @DisplayName("Demonstrate False Sharing")
    void demonstrateFalseSharing() throws InterruptedException {
        for (int i = 1; i <= numOfThreads; i++) {
            final Thread[] threads = new Thread[i];
            for (int j = 0; j < threads.length; j++) {
                threads[j] = new Thread(createUnpaddedRunnable(j));
            }
            final Instant start = Instant.now();
            for (final Thread t : threads) {
                t.start();
            }
            for (final Thread t : threads) {
                t.join();
            }
            final long timeElapsed = (Duration.between(start, Instant.now()).toMillis());
            System.out.printf("[UNPADDED] No of Threads=[%d], total time taken: %d ms%n%n", i, timeElapsed);
        }
        System.out.println("------------------------------");
    }

    @Test
    @DisplayName("Demonstrate fixing False Sharing by padding")
    void demonstrateFixingFalseSharingByPadding() throws InterruptedException {
        for (int i = 1; i <= numOfThreads; i++) {
            final Thread[] threads = new Thread[i];
            for (int j = 0; j < threads.length; j++) {
                threads[j] = new Thread(createPaddedRunnable(j));
            }
            final Instant start = Instant.now();
            for (final Thread t : threads) {
                t.start();
            }
            for (final Thread t : threads) {
                t.join();
            }
            final long timeElapsed = (Duration.between(start, Instant.now()).toMillis());
            System.out.printf("[PADDED] No of Threads=[%d], total time taken: %d ms%n%n", i, timeElapsed);
        }
        System.out.println("------------------------------");
    }

    private Runnable createUnpaddedRunnable(final int k) {
        return () -> {
            long i = iterations + 1;
            while (0 != --i) {
                unPaddedLongs[k].value = i;
            }
        };
    }

    private Runnable createPaddedRunnable(final int k) {
        return () -> {
            long i = iterations + 1;
            while (0 != --i) {
                paddedLongs[k].value = i;
            }
        };
    }

}
```

Output:

```
[UNPADDED] No of Threads=[1], total time taken: 866 ms

[UNPADDED] No of Threads=[2], total time taken: 2828 ms

[UNPADDED] No of Threads=[3], total time taken: 4532 ms

[UNPADDED] No of Threads=[4], total time taken: 5207 ms

------------------------------
[PADDED] No of Threads=[1], total time taken: 966 ms

[PADDED] No of Threads=[2], total time taken: 1074 ms

[PADDED] No of Threads=[3], total time taken: 1261 ms

[PADDED] No of Threads=[4], total time taken: 1504 ms

------------------------------
```

Thus, to summarize how to write correct concurrent code in multicore CPU environment:

- Check for **race conditions**
    - multiple threads are reading / writing a given field
    - it can only occur on class member fields (not local member variables or method parameters)
- Check for **happens-before** relationship
    - if more than one field or lines are part of critical section = use **synchronization**
    - if only one field needs to be visible (with no compound actions) = use **volatile**

#### Interview Problem 7 (JP Morgan Chase, Merrill Lynch, Goldman Sachs): Singleton Design Pattern

**Singleton Pattern** says that just "define a class that has only one instance and provides a global point of access to
it". In other words, a class must ensure that only single instance should be created and single object can be used by
all other classes.

However, writing a Singleton pattern class in Java can be tricky in concurrent multicore environment.

First implementation of Singleton pattern which will work fine **only when a single thread** is using it.

```java
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
```

As we can see that there would be race condition when multiple threads are doing read / write operation on the instance
variable. So this implementation is NOT thread safe.

Let's make the design thread safe using **synchronized**.

```java
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
```

Now our design is thread safe, but it is NOT performance efficient.

In multicore CPU architecture, the multiple threads running on multiple cores have to wait even for **read** operation:
`if (instance == null)`. However, in read-write lock paradigm, multiple threads can READ the critical section unless
there is any other thread WRITING on that critical section.

Using the above design - it will be affecting performance as multiple threads have to wait for the read operation
everytime even though the instance has already been instantiated.

Let's take the read operation outside the synchronized block to support multiple reads simultaneously by multiple
threads. This is called **"Double Check Locking Singleton Pattern"**.

```java
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
```

In the first look, it seems perfect and solve our multiple read problem - however, this code is BUGGY!

We have a **non-synchronized** READ supposed to return the value set by a **synchronized** WRITE. We do NOT have the
guarantee that the READ will get the value set by the WRITE. For that, we have learned before that it needs a
**"happens-before"** relationship, and we do not have it in the above code.

In simpler words, **"Double Check Locking Singleton Pattern"** is buggy in multicore CPU architecture because there is
no **"happens-before"** relationship between the READ returning the value and the WRITE that sets it.

**How to fix it?**

By creating a **"happens-before"** relationship between the READ returning the value and the WRITE that sets it.

And we have learnt it that we can use **volatile** or **synchronized** block to do it.

```java
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
```

Marking the `instance` as **volatile** will create the **"happens-before"** relationship between the READ returning the
value and the WRITE that sets it.

Although we have fixed the **"Double Check Locking Singleton Pattern"** but again we are at the same performance issues
which we encountered using just **synchronized** version of the code.

The MOST correct, clean and concise implementation of Singleton design pattern is to use **ENUM**.

```java
public enum Singleton {
    INSTANCE
}
```

However, note that we can't use this approach if our singleton must extend a superclass other than `java.lang.Enum`,
though we can declare an `enum` to implement interfaces.

The same way it is used all over JDK APIs, for ex:

```java
enum NaturalOrderComparator implements Comparator<Comparable<Object>> {
    INSTANCE;

    @Override
    public int compare(final Comparable<Object> c1, final Comparable<Object> c2) {
        return c1.compareTo(c2);
    }

    @Override
    public Comparator<Comparable<Object>> reversed() {
        return Comparator.reverseOrder();
    }
}
```

And called in `Comparator` interface as **static** method:

```
public static <T extends Comparable<? super T>> Comparator<T> naturalOrder() {
        return (Comparator<T>) Comparators.NaturalOrderComparator.INSTANCE;
}
```

---

## Module 2 - Advanced Concurrency

### Chapter 03. Executor Pattern, Callable and Future

#### Executor Pattern

The naive way to create a thread and start is by using `Runnable` interface.

Code snippet:

```
final Runnable task = () -> System.out.println("Hello students!");
final Thread thread = new Thread(task);
thread.start();
```

- the thread task is wrapped in an instance of Runnable `run()` method
- Runnable instance is passed to a new instance of Thread in constructor
- Thread is executed by calling `start()`

Downsides of this approach:

- a thread is created on demand by the user - means that any user can create any number of threads in the application
- **problem**: most of the operating systems have a limit to the maximum number of threads which can be created
- once the task is done, i.e. finish the `run()` method => the thread dies
- **problem**: a thread is an expensive resource, and it will impact system performance if too many threads are created
  and finished-terminated at a quick rate

The **Executor Pattern** aims to fix the above-mentioned issues:

- by creating pools of ready-to-use threads
- passing task to this pool of threads that will execute it

```java
public interface Executor {
    void execute(Runnable task);
}
```

A pool of thread is an instance of the `Executor` interface.

```java
public interface ExecutorService extends Executor {
    // 11 more methods
}
```

`ExecutorService` is an extension of `Executor` and has got around 12 more methods. The implementations of both
interfaces are the same. The factory class `Executors` proposes around 20 more methods to **create executors**.

Code snippet to create a single-thread pool:

```
final ExecutorService executor = Executors.newSingleThreadExecutor();
final Runnable task = () -> System.out.println("Hello students!");

// Executor pattern
executor.execute(task);

// Runnable pattern
new Thread(task).start();
```

The thread in this pool will be kept alive as long as this pool is alive. It means that the single thread will execute
the submitted task => once the task finishes, the thread will return to the pool and wait for new task to be submitted
for execution.

As compared to Runnable pattern, Executor pattern does NOT create a new thread. However, the behavior is same: both
calls return immediately and task is executed in **another** thread.

We can also create executors with multiple threads in the pool:

```
// fixed thread pool size of 4 threads
final ExecutorService multipleThreadsExecutor = Executors.newFixedThreadPoolExecutor(4);

// cached thread pool where size is dependent on number of system cpu cores available 
// creates new threads as needed, but will reuse previously constructed threads when they are available
final ExecutorService cachedExecutor = Executors.newCachedThreadPool();
```

More about **cached** thread pool:

- create threads **on demand** but will reuse previously constructed threads when they are available
- keeps **unused** threads for **60 seconds** by default, then terminates them

**Waiting queue**

Suppose we have a code snippet:

```
final Executor executor = Executors.newSingleThreadExecutor();

final Runnable task1 = () -> someLongProcess();
final Runnable task2 = () -> anotherLongProcess();

executor.execute(task1);
executor.execute(task2);
```

When we run this code, **task2** has to wait for **task1** to complete. The executor has a **waiting queue** to handle
this:

- A task is added to the waiting queue when **no** thread is available
- The tasks are executed in the **order** of their submission

**Can we know if a task is done or not?**

**Answer is no** - we don't have any API method in **ExecutorService** to check that. However, we can have a print
statement at the end of our Runnable task (`run()` method) to indicate that task has completed.

**Can we cancel the execution of a task?**

**Answer is yes** - BUT only if the task has NOT started yet and still in the waiting queue.

To summarize the advantages of using **Executor Pattern**:

- building an executor is **more performance efficient** than creating threads on demand
- can pass instances of Runnable to an executor - even if there are no threads available in the pool, executor has a
  waiting queue to **handle more tasks** than number of threads in the pool
- a task can be cancelled by removing it from the waiting queue

#### Callable and Future

There are some caveats in `Runnable` interface:

```java

@FunctionalInterface
public interface Runnable {
    void run();
}
```

There is no way we can know if a task is done or not.

- No object can be returned
- No exception can be raised

We need to know if there was any exception raised by the task and also we may need flexibility to get a value returned
from the task.

In Java 1.5 release, new `Callable` interface was introduced:

```java

@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
```

As seen, `Callable` can return `V` object and also throws `Exception` (if any) to the calling thread.

We can use it via `submit()` method in `ExecutorService` which returns a `Future` object:

```
<T> Future<T> submit(Callable<T> task);
```

Sample code snippet:

```
// In the main thread
final ExecutorService executor = Executors.newFixedThreadPoolExecutor(4);
final Callable<String> task = () -> someTaskWhichReturnsString();

final Future<String> future = executor.submit(task); // asynchronous
// optionally, main thread can do some other task here
// ...
final String result = future.get(); // get() call is blocking until the returned String is available 
```

More about `Future.get()` method:

- blocking until the returned value `V` is available
- can raise 2 exceptions:
    - if the thread from the thread pool in `ExecutorService` is interrupted => throws `InterruptedException`
    - if the submitted task itself throws an exception => it is wrapped in an `ExecutionException` and re-thrown

We can use overloaded `Future.get()` methods which allows to pass a **timeout** as argument to avoid indefinitely
blocking calls. However, if the value `V` is still not available after the given timeout, it will throw
`TimeoutException`.

Other methods in `Future`:

- **isDone()**: tells us if the executor has finished processing the task. If the task is complete, it will return true;
  otherwise, it returns false.
- **cancel(boolean)**: can be used to tell the executor to stop the operation and interrupt its underlying thread.
- **isCancelled()**: tells us if the task is cancelled or not, returning a boolean.

Now as we have learnt basics of `Callable` and `Future`, let's explore more methods available in `Executor`.

ExecutorService can execute both `Runnable` and `Callable` tasks.

Example code snippet:

```
final Runnable runnableTask = () -> {
    try {
        TimeUnit.MILLISECONDS.sleep(300L);
        System.out.println("Hello students from Runnable!");
    } catch (final InterruptedException e) {
        e.printStackTrace();
    }
};

final Callable<String> callableTask = () -> {
    TimeUnit.MILLISECONDS.sleep(300L);
    return "Hello students from Callable!";
};

final List<Callable<String>> callableTasks = List.of(callableTask, callableTask, callableTask);
final ExecutorService executorService = Executors.newFixedThreadPoolExecutor(2);
```

We can call following methods:

- **execute()**: method is `void` and doesn't give any possibility to get the result of a task's execution or to check
  the task's status (if it is running):
    - `executorService.execute(runnableTask);`
- **submit()**: submits a `Callable` or a `Runnable` task to an `ExecutorService` and returns a result of type `Future`:
    - `Future<String> future = executorService.submit(callableTask);`
    - `Future<?> future = executorService.submit(runnableTask);` // Future's `get()` method will return `null` upon
      successful completion
- **invokeAny()**: assigns a collection of tasks to an `ExecutorService`, causing each to run, and returns the result of
  a successful execution of **one** task (if there was a successful execution)
    - `String result = executorService.invokeAny(callableTasks);`
- **invokeAll()**: assigns a collection of tasks to an `ExecutorService`, causing each to run, and returns the result of
  **all** task executions in the form of a list of objects of type `Future`
    - `List<Future<String>> futures = executorService.invokeAll(callableTasks);`

**Shutting Down an ExecutorService**

The `ExecutorService` will not be automatically destroyed when there is no task to process. It will stay alive and wait
for new work to do. So, the application or main thread will not terminate as the threads in the executor's thread pool
are **non-daemon** which will keep the application alive.

To properly shut down an `ExecutorService`, we have the `shutdown()` and `shutdownNow()` methods.

- **shutdown()**: method doesn't cause immediate destruction of the `ExecutorService`. It will make the
  `ExecutorService` stop accepting new tasks and shut down after all running threads finish their current work.
    - `executorService.shutdown();`
- **shutdownNow()**: method tries to destroy the `ExecutorService` immediately, but it doesn't guarantee that all the
  running threads will be stopped at the same time.
    - `List<Runnable> notExecutedTasks = executorService.shutDownNow();` // This method returns a list of tasks that are
      waiting to be processed. It is up to the developer to decide what to do with these tasks.

**Oracle** recommended way to shut down the `ExecutorService` using `awaitTermination()` method:

```
executorService.shutdown();
try {
    if (!executorService.awaitTermination(900L, TimeUnit.MILLISECONDS)) {
        executorService.shutdownNow();
    } 
} catch (final InterruptedException e) {
    executorService.shutdownNow();
}
```

`ExecutorService` will first stop taking new tasks and then wait up to a specified period of time (900 ms) for all tasks
to be completed. If that time expires, the execution is stopped immediately.

#### Interview Problem 8 (Barclays): Implement GCD algorithm using Executor Service and Futures

Greatest Common Divisor (GCD) of two or more integers is the largest integer that divides each of the integers such that
their remainder is zero.

Example:

- GCD of 20, 30 = 10  (10 is the largest number which divides 20 and 30 with remainder as 0)
- GCD of 42, 120, 285 = 3  (3 is the largest number which divides 42, 120 and 285 with remainder as 0)

Pseudo Code of the Euclidean Algorithm to find GCD of 2 numbers:

- Step 1:  Let `a, b` be the two numbers
- Step 2:  `a mod b = R`
- Step 3:  Let `a = b` and `b = R`
- Step 4:  Repeat Steps 2 and 3 until `a mod b` is greater than `0`
- Step 5:  `GCD = a`
- Step 6: Finish

Write the GCD algorithm in Java using Executor Service and Futures.

**Solution**:

Let's implement the GCD algorithm as given in the pseudocode:

```
    // Euclidean Algorithm
    private static int gcd(final int a, final int b) {
        if (b == 0)
            return a;

        return gcd(b, a % b);
    }
```

Complete code using Futures:

```java
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GCDCalculator {

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public Future<Integer> calculate(final int a, final int b) {
        return executor.submit(() -> {
            Thread.sleep(1000L);
            return gcd(a, b);
        });
    }

    // Euclidean Algorithm
    private static int gcd(final int a, final int b) {
        if (b == 0)
            return a;

        return gcd(b, a % b);
    }

    private void shutdown() {
        executor.shutdown();
    }

    public static void main(final String[] args) throws InterruptedException, ExecutionException {
        final GCDCalculator gcdCalculator = new GCDCalculator();

        final Future<Integer> future1 = gcdCalculator.calculate(20, 30);
        final Future<Integer> future2 = gcdCalculator.calculate(15, 35);

        while (!(future1.isDone() && future2.isDone())) {
            System.out.printf("future1 is %s and future2 is %s%n",
                              future1.isDone() ? "done" : "not done",
                              future2.isDone() ? "done" : "not done");
            Thread.sleep(300L);
        }

        final Integer result1 = future1.get();
        final Integer result2 = future2.get();

        System.out.printf("GCD of (20,30) is %d%n", result1);
        System.out.printf("GCD of (15,35) is %d%n", result2);

        gcdCalculator.shutdown();
    }
}
```

Output:

```
future1 is not done and future2 is not done
future1 is not done and future2 is not done
future1 is not done and future2 is not done
future1 is not done and future2 is not done
GCD of (20,30) is 10
GCD of (15,35) is 5
```

**ScheduledExecutorService**

The `ScheduledExecutorService` runs tasks after some predefined delay and/or periodically. We can create a new
`ScheduledExecutorService` instance using the `Executors` factory method:

```
ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
```

Methods available:

- **schedule()**: schedule a single task's execution after a fixed delay; tasks can be `Callable` or `Runnable`
    - `Future<String> resultFuture = executorService.schedule(callableTask, 1, TimeUnit.SECONDS);` // delays for one
      second before executing **callableTask**
- **scheduleAtFixedRate()**: schedule a single task's execution after a fixed delay and then keep on running at a fixed
  rate. Following block of code will run a task after an initial delay of 100 ms and after that, it will run the same
  task every 450 ms:
    - `executorService.scheduleAtFixedRate(runnableTask, 100L, 450L, TimeUnit.MILLISECONDS);`
- **scheduleWithFixedDelay()**: if it is necessary to have a fixed length delay between iterations of the task.
  Following code will guarantee a 150 ms pause between the end of the current execution and the start of another one:
    - `executorService.scheduleWithFixedDelay(task, 100, 150, TimeUnit.MILLISECONDS);`

**Few common pitfalls of ExecutorService**

- Keeping an unused `ExecutorService` alive: We need to be careful and shut down an ExecutorService, otherwise the
  application will keep on running even if it has completed.
- Wrong thread-pool capacity while using fixed length thread pool: It is very important to determine how many threads
  the application will need to run tasks efficiently. A too-large thread pool will cause unnecessary overhead just to
  create threads that will mostly be in the waiting mode. Too few can make an application seem unresponsive because of
  long waiting periods for tasks in the queue.
- Calling a Future‘s `get()` method after task cancellation: Attempting to get the result of an already canceled task
  triggers a `CancellationException`.
- Unexpectedly long blocking with Future‘s `get()` method: We should use timeouts to avoid unexpected waits.

---

### Chapter 04. Fork/Join Framework

The Fork/Join Framework was designed to recursively split a parallelizable task into smaller tasks and then combine the
results of each subtask to produce the overall result. It provides tools to help speed up parallel processing by
attempting to use all available processor cores. It accomplishes this through a divide and conquer approach.

Pseudo code:

```
if (task is small enough or no longer divisible) {
    compute task sequentially
} else {
    split task in 2 subtasks
    call this method recursively possibly further splitting each subtask
    wait for the completion of all subtasks
    combine the results of each subtask
}
```

In practice, this means that the framework first **"forks"** recursively breaking the task into smaller independent
subtasks until they are simple enough to run asynchronously.

After that, the **"join"** part begins. The results of all subtasks are recursively joined into a single result. In the
case of a task that returns `void`, the program simply waits until every subtask runs.

To provide effective parallel execution, the fork/join framework uses a pool of threads called the `ForkJoinPool`. This
pool manages worker threads of type `ForkJoinWorkerThread`.

**ExecutorService vs Fork/Join**

After the release of Java 7, many developers decided to replace the `ExecutorService` framework with the **Fork/Join
framework**.

However, despite the simplicity and frequent performance gains associated with fork/join, it reduces developer control
over concurrent execution.

`ExecutorService` gives the developer the ability to control the number of generated threads and the granularity of
tasks that should be run by separate threads. The best use case for `ExecutorService` is the processing of
**independent** tasks, such as transactions or requests according to the scheme **"one thread for one task"**.

In contrast, **fork/join** was designed to speed up work that can be broken into smaller pieces recursively.