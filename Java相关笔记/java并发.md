[toc]

# java并发

# 一、线程状态的转换

![线程转换](../../../youdaonote-images/3782D564310F4EFD9D604DF102DD2897.jpeg)

## 新建（New）

创建后尚未运行。

## 可运行（Runnable）

可能正在运行，也可能正在等待CPU时间片。  
包括操作系统线程状态中的Running和Ready。  
Running：线程获取CPU权限进行执行。

## 阻塞（Blocking）

等待获取一个排它锁，如果其线程释放了锁就会结束此状态。

## 无限期等待（Waiting）

等待其它线程显式地唤醒，否则不会被分配 CPU 时间片。
|进入方法|退出方法|
|---|---|
|没有设置 Timeout 参数的 Object.wait() 方法|Object.notify() / Object.notifyAll()|
|没有设置 Timeout 参数的 Thread.join() 方法|被调用的线程执行完毕|
|LockSupport.park() 方法|LockSupport.unpark(Thread)|

## 期限等待（Timed Waiting）

无需等待其他线程显示地唤醒，在一定时间之后会被系统自动唤醒。

调用 Thread.sleep() 方法使线程进入限期等待状态时，常常用“使一个线程睡眠”进行描述。

调用 Object.wait() 方法使线程进入限期等待或者无限期等待时，常常用“挂起一个线程”进行描述。

睡眠和挂起是用来描述行为，而阻塞和等待用来描述状态。

阻塞和等待的区别在于，阻塞是被动的，它是在等待获取一个排它锁。而等待是主动的，通过调用 Thread.sleep() 和 Object.wait() 等方法进入。
|进入方法|退出方法|
|---|---|
|Thread.sleep() 方法|时间结束|
|设置了 Timeout 参数的 Object.wait() 方法|时间结束 / Object.notify() / Object.notifyAll()|
|设置了 Timeout 参数的Thread.join() 方法|时间结束 / 被调用的线程执行完毕|
|LockSupport.parkNanos() 方法|时间结束 / LockSupport.unpark(Thread)|
|LockSupport.parkUntil() 方法|时间结束 / LockSupport.unpark(Thread)|

## 死亡（Terminated）

可以是线程结束任务之后自己结束，或者产生了异常而结束。

# 二、使用线程

三种使用线程的方式：

* 实现Runnable接口；

  ```java
    // 执行
    MyRunnable myRunnable = new MyRunnable();
    Thread thread1 = new Thread(myRunnable);
    thread1.start();
  ```

* 实现Callable接口（可以有返回值）；

    ```java
    public static class MyCallable implements Callable<Integer>{

        @Override
        public Integer call() throws Exception {
            Integer i = 0;
            while (i < 3){
                Thread.sleep(1000);
                i++;
            }
            return i;
        }
    }
    // 执行
    MyCallable myCallable = new MyCallable();
    FutureTask<Integer> ft = new FutureTask<>(myCallable);
    Thread thread2 = new Thread(ft);
    thread2.start();
    while (!ft.isDone()){
        System.out.println(ft.get());
    }
    ```

* 继承Thread类。

实现Runnable和Callable接口的类不是真正意义上的线程，还需要通过Thread来调用。可以说任务是通过线程驱动从而执行的。

## 实现接口 VS 继承Thread

实现会更好一些，原因：

* Java不支持多继承，继承Thread不能再去继承别的类，但是可以实现多个接口。
* 类可能只要求可执行就行，继承整个Thread类开销过大。

# 四、基础线程机制

## Executor

线程池，管理多个线程执行，而无需程序员显式地管理线程的生命周期。

主要有三种Executor：

* CachedThreadPool: 一个任务创建一个线程。
* FixedThreadPool: 固定大小的线程池。
* SingleThreadExecutor：相当于大小为 1 的 FixedThreadPool。

```java
ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
for (int i = 0; i < 5; i++) {
    cachedThreadPool.execute(new MyRunnable());
}
cachedThreadPool.shutdown();
```

底层调用的是ThreadPoolExecutor方法，传入一个同步的阻塞队列实现缓存。

### newCachedThreadPool

```java
ExecutorService pool = Executors.newCachedThreadPool();
```

* 重用之前的线程
* 适合执行许多短期的异步任务
* 如果没有可用的线程，则创建一个，最大线程数是Integer.MAX_VALUE
* 默认为60s未使用就被终止和移除
* 长期闲置的池将会不消耗任何资源

Executors源码：

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
}
```

### newWorkStealingPool

jdk1.8添加，根据需要的并行层动态创建和关闭线程，底层使用ForkJoinPool实现（jdk1.7）。
ForkJoinPool的优势在于，可以充分利用多cpu，多核cpu的优势，把一个任务拆分成多个“小任务”，把多个“小任务”放到多个处理器核心上并行执行；当多个“小任务”执行完成之后，再将这些执行结果合并起来即可。使用一个无限队列来保存需要执行的任务，可以传入线程的数量，不传入，则默认使用当前计算机中可用的cpu数量，使用分治法来解决问题，使用fork()和join()来进行调用。适合大规模长时间的的任务执行。

```java
// 没有传入线程数，当前计算机可用的CPU数量设置为线程数量。
public static ExecutorService newWorkStealingPool() {
    return new ForkJoinPool
            (Runtime.getRuntime().availableProcessors(),
             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
             null, true);
}

public static ExecutorService newWorkStealingPool(int parallelism) {
    return new ForkJoinPool
            (parallelism,
             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
             null, true);
}
```

计算传入的数据和，示例：

```java
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int[] arr = new int[100];
        for (int i = 0; i < 100; i++) {
            arr[i] = i;
        }
        ForkJoinPoolTest forkJoinPoolTest = new ForkJoinPoolTest();
        SumTask sumTask = forkJoinPoolTest.new SumTask(arr, 0, arr.length);
        ForkJoinPool executorService = (ForkJoinPool) Executors.newWorkStealingPool();
        ForkJoinTask<Integer> submit = executorService.submit(sumTask);
        System.out.println("多线程执行结果："+submit.get());
        executorService.shutdown();
    }


    class SumTask extends RecursiveTask<Integer>{
        private static final int SPLIT_NUMBER = 20;
        private int[] array;
        private int start;
        private int end;

        public SumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            int sum = 0;
            if(end - start < SPLIT_NUMBER){
                for(int i= start;i<end;i++){
                    sum += array[i];
                }
                return sum;
            }else {
                int middle = (start+ end)/2;
                SumTask startSumTask = new SumTask(array, start, middle);
                SumTask endSumTask = new SumTask(array, middle, end);
                //并行执行两个 小任务
                startSumTask.fork();
                endSumTask.fork();
                // 获取结果
                return startSumTask.join() + endSumTask.join();
            }
        }
    }
```

### newSingleThreadExecutor

* 任何情况下都不会超过一个任务处于运行状态
* 与newFixedThreadPool(1)不同是不能重新配置加入线程，使用FinalizableDelegatedExecutorService进行包装
* 按照任务提交顺序执行。
* 当线程执行中出现异常，去创建一个新的线程替换

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
}
```

### newFixedThreadPool

* 可重用固定线程数的线程池
* 当所有线程都处于活动状态时，如果提交了其他任务，他们将在队列中等待一个线程可用
* 线程会一直存在，直到调用shutdown

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
}
```

```java
public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>(),
                                      threadFactory);
}
```

**注意：** newFixedThreadPool使用的无边界队列，当生产者大于消费者时，会出现大量阻塞的任务，占用内存。

### newScheduledThreadPool

* 一个定长线程池，支持定时及周期性任务执行。
* 空闲线程会进行保留

```java
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
    return new ScheduledThreadPoolExecutor(corePoolSize);
}

public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
    return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
}
```

ScheduledExecutorService，底层调用了ThreadPoolExecutor，维护了一个延迟队列，可以传入线程数量，传入延时的时间等参数。

```java
public ScheduledThreadPoolExecutor(int corePoolSize) {
    super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
              new DelayedWorkQueue());
}
```
DelayedWorkQueue是一个无界队列，它能按一定的顺序对工作队列中的元素进行排列。

调用示例：

```java
public static void main(String[] args) {
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    for (int i = 0; i < 15; i++) {
        scheduledExecutorService.schedule(() -> {
                System.out.println("执行时间: "+ new Date());
            }, i, TimeUnit.SECONDS);
    }
    scheduledExecutorService.shutdown();
}
```

### 应用场景

* newCachedThreadPool：适用于服务器负载较轻，执行大量短期异步任务。
* newFixedThreadPool：采用无界的阻塞队列，所以实际线程数量永远不会变化，适用于可以预测线程数量的业务中，或者服务器负载较重，对当前线程数量进行限制。**注意任务阻塞过多占用大量内存问题**
* newSingleThreadExecutor：适用于需要保证顺序执行各个任务，并且在任意时间点，不会有多个线程是活动的场景。
* newScheduledThreadPool：可以延时启动，定时启动的线程池，适用于需要多个后台线程执行周期任务的场景。
* newWorkStealingPool：创建一个拥有多个任务队列的线程池，可以减少连接数，创建当前可用cpu数量的线程来并行执行，适用于大耗时的操作，可以并行来执行

## Daemon

守护线程是程序运行时在后台提供服务的线程，不属于程序中不可或缺的部分，比如垃圾回收线程。

当所有的非守护线程结束时，程序也就结束了，同时也会杀死进程中的所有守护线程。
main() 属于非守护线程。

通过使用Thread对象的setDeamon(true)方法将线程设置为守护线程，注意：

1. thread.setDeamon(true)必须在thread.start()之前设置，否则会抛出IllegalThreadStateException异常。不可以将正在运行的线程设置为守护线程。
2. 在Deamon线程中产生的线程也是Deamon的。
3. 守护线程应该永远不去访问固有资源，如文件、数据库，因为它会在任何时候甚至在一个操作的中间发生中断。

## sleep()

Thread.sleep(millisec) 方法会休眠当前正在执行的线程，millisec 单位为毫秒。

sleep() 可能会抛出 InterruptedException，因为异常不能跨线程传播回 main() 中，因此必须在本地进行处理。线程中抛出的其它异常也同样需要在本地进行处理。

```java
public void run() {
    try {
        Thread.sleep(3000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```

## yield()

`Thread.yield();`译为线程让步，让当前线程从运行状态进入就绪状态，它会把CPU执行的时间让掉，让其他或者**自己**线程执行。

# 四、线程的中断

一个线程执行完毕之后会自动结束，如果在运行中发生异常也会提前结束。

## 中断协议

每个线程都有一个与线程是否已中断的相关联的Boolean属性，用于表示线程的中断状态（interrupted status）。中断状态初始时为false；当一个线程A通过调用threadB.interrupt()中断线程B时，会出现以下两种情况之一:

* 如果那个线程B在执行一个低级可中断阻塞方法，例如 Thread.sleep()、 Thread.join() 或 Object.wait()，那么它将取消阻塞并抛出 InterruptedException。但是不能中断 I/O 阻塞和 synchronized 锁阻塞。
* 否则，interrupt()只是设置线程B的中断状态。 在被中断线程B中运行的代码以后可以轮询中断状态，看看它是否被请求停止正在做的事情。中断状态可以通过 Thread.isInterrupted() 来读取，并且可以通过一个名为 Thread.interrupted() 的操作读取和清除。

## interrupt()

发送中断信号
对于以下代码，在 main() 中启动一个线程之后再中断它，由于线程中调用了 Thread.sleep() 方法，因此会抛出一个 InterruptedException，从而提前结束线程，不执行之后的语句。

```java
public static void main(String[] args) {
    Thread thread = new Thread(){
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                System.out.println("Thread run");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    thread.start();
    thread.interrupt();
}
/*输出
//java.lang.InterruptedException: sleep interrupted
at java.lang.Thread.sleep(Native Method)
at com.wkk.demo.javaconcurrent.InterruptTest$1.run(InterruptTest.java:15)
*/
```

## interrupted()

获取线程是否已经中断。在线程中可以通过循环判断是否发出中断信号，决定当前线程是否停止。

## Executor的中断操作

调用Executor的shutdown()方法会等待线程都执行完毕之后再关闭，但是调用shutdownNow()方法，相当于调用了每个线程的interrupt()方法。

只想中断一个线程，可以通过使用submit()方法提交一个线程，它会返回一个 Future<?> 对象，通过调用该对象的 cancel(true) 方法就可以中断线程。

```java
Future<?> future = executorService.submit(() -> {
    // ..
});
future.cancel(true);
```

### Future简介

Future接口用于获取异步计算的结果，可通过get()获取结果、cancel()取消、isDone()判断是否完成等操作。[源码参考](https://blog.csdn.net/codershamo/article/details/51901057)
方法：

* V get()： 获取结果，若无结果会阻塞至异步计算完成
* V get(long timeOut, TimeUnit unit)：获取结果，超时返回null
* boolean isDone()：执行结束（完成/取消/异常）返回true
* boolean isCancelled()：任务完成前被取消返回true
* boolean cancel(boolean mayInterruptRunning)：取消任务，未开始或已完成返回false，参数表示是否中断执行中的线程

当cancel()方法参数为false时，只能取消还没有开始的任务，若任务已经开始了，就任由其运行下去
当创建了Future实例，任务可能有以下三种状态：

* 等待状态。此时调用cancel()方法不管传入true还是false都会标记为取消，任务依然保存在任务队列中，但当轮到此任务运行时会直接跳过。
* 完成状态。此时cancel()不会起任何作用，因为任务已经完成了。
* 运行中。此时传入true会中断正在执行的任务，传入false则不会中断。
  
### CAS和Unsafe类

[参考](https://blog.csdn.net/javazejian/article/details/72772470)

# 五、互斥同步

Java提供了两种锁机制来控制对共享资源的互斥访问，一个是JVM实现的synchronized，一个是JDK实现的ReentrantLock。

## synchronized

**1. 同步一个代码块**

```java
public void test(){
    //...
    synchronized(this){
        //...
    }
    //..
}
```

只能实现同一对象对代码块的访问互斥。
**2. 同步一个方法**
**3. 同步一个类**

```java
public void test(){
    //...
    synchronized(SynchronizedExample.class)){
        //...
    }
    //...
}
```

作用于整个类，也就是说两个线程调用同一个类的不同对象上的这种同步语句，也会进行同步。
**4. 同步一个静态方法**

```java
public synchronized void test(){
    //...
}
```

作用于整个类

### 对象锁（monitor）机制

执行同步代码块后首先要先执行monitorenter指令，退出的时候执行monitorexit指令。使用Synchronized进行同步，其关键就是必须要对对象的监视器monitor进行获取，当线程获取monitor后才能继续往下执行，否则就只能等待。而这个获取的过程是互斥的，即同一时刻只有一个线程能够获取到monitor。Synchronized先天具有重入性。每个对象拥有一个计数器，当线程获取该对象锁后，计数器就会加一，释放锁后就会将计数器减一。

任意一个对象都拥有自己的监视器，当这个对象由同步块或者这个对象的同步方法调用时，执行方法的线程必须先获取该对象的监视器才能进入同步块和同步方法，如果没有获取到监视器的线程将会被阻塞在同步块和同步方法的入口处，进入到BLOCKED状态。
![对象锁获取](../../../youdaonote-images/72D215D872BC4B48BC9FB226862858EB.jpeg)

> 引申 [happens-before规则](https://juejin.im/post/5ae6d309518825673123fd0e#heading-3)

### synchronized优化

synchronized最大的特征就是在同一时刻只有一个线程能够获得对象的监视器（monitor），从而进入到同步代码块或者同步方法之中，即表现为互斥性（排它性）。一次只能一个线程效率低下

#### CAS操作

CAS操作（又称为无锁操作）是一种乐观锁策略，它假设所有线程访问共享资源的时候不会出现冲突，既然不会出现冲突自然而然就不会阻塞其他线程的操作。通过使用compare and swap（比较交换），解决冲突。

CAS比较交换的过程可以通俗的理解为CAS(V,O,N)，包含三个值分别为：V 内存地址存放的实际值；O 预期的值（旧值）；N 更新的新值。如果与O相同就表明没有更改，直接更新为N，如果如O不相等，更新失败。

CAS的实现需要硬件指令集的支撑，在JDK1.5后虚拟机才可以使用处理器提供的CMPXCHG指令实现。

> ABA问题：通过变量值加入版本号解决
> [参考](https://juejin.im/post/5ae6dc04f265da0ba351d3ff#heading-5)

## ReentrantLock

ReentrantLock 是 java.util.concurrent（J.U.C）包中的锁。支持重入性，表示能够对共享资源能够重复加锁，即当前线程获取该锁再次获取不会被阻塞。ReentrantLock还支持公平锁和非公平锁两种方式。

### 1.主要方法

* lock();  获得锁
* lockInterruptibly();  获得锁，但优先响应中断
* tryLock();    尝试获得锁，成功返回true,否则false，该方法不等待，立即返回
* tryLock(long time,TimeUnit unit);   在给定时间内尝试获得锁
* unlock();     释放锁

### 2.重入性的实现原理

要想支持重入性，就要解决两个问题：1. 在线程获取锁的时候，如果已经获取锁的线程是当前线程的话则直接再次获取成功；2. 由于锁会被获取n次，那么只有锁在被释放同样的n次之后，该锁才算是完全释放成功。

以非公平锁为例，判断当前线程能否获得锁为例，核心方法为nonfairTryAcquire：

```java
final boolean nonfairTryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```

分析：如果当前没有线程占用，直接设置当前线程占用。存在线程占用并且占用线程为当前申请锁的线程时，同步状态加1（state+1），返回true。

以非公平锁为例，释放锁，核心方法为tryRelease：

```java
protected final boolean tryRelease(int releases) {
    int c = getState() - releases;
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
        free = true;
        setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}
```

分析：重入锁的释放必须得等到同步状态为0时锁才算成功释放，否则锁仍未释放。如果锁被获取n次，释放了n-1次，该锁未完全释放返回false，只有被释放n次才算成功释放，返回true。

### 3.公平锁与非公平锁

ReentrantLock支持两种锁：公平锁和非公平锁。何谓公平性，是针对获取锁而言的，如果一个锁是公平的，那么锁的获取顺序就应该符合请求上的绝对时间顺序，满足FIFO（先进先出）。

```java
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

公平锁的tryAcquire方法

```java
protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (!hasQueuedPredecessors() &&
            compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```

分析：逻辑与nonfairTryAcquire基本上一直，唯一的不同在于增加了hasQueuedPredecessors的逻辑判断，该方法是用来判断当前节点在同步队列中是否有前驱节点的判断，如果有前驱节点说明有线程比当前线程更早的请求资源，根据公平性，当前线程请求资源失败。如果当前节点没有前驱节点的话，再才有做后面的逻辑判断的必要性。公平锁每次都是从同步队列中的第一个节点获取到锁，而非公平性锁则不一定，有可能刚释放锁的线程能再次获取到锁。
>比较

* 公平锁每次获取锁为同步队列中的第一个节点，保证请求资源时间上的绝对顺序，而非公平锁不保证这一点，可能造成“饥饿”现象。
* 公平锁为了保证时间上的绝对顺序，需要频繁的上下文切换，而非公平锁会降低一定的上下文切换，降低性能开销。因此，ReentrantLock默认选择的是非公平锁，则是为了减少一部分上下文切换，保证了系统更大的吞吐量。

> [参考](https://blog.csdn.net/u011521203/article/details/80186741)；[源码阅读参考](https://www.cnblogs.com/zhimingyang/p/5702752.html)

### 4.Condition

Lock 替代了 synchronized 方法和语句的使用，Condition（await、signal和signalAll） 替代了 Object（wait、notify 和 notifyAll） 监视器方法的使用。

新建Condition对象，一个ReentrantLock可以绑定多个Condition对象

```java
Condition condition = lock.newCondition();
```

* Condition与Object中的wati,notify,notifyAll区别：

    * Object中的这些方法是和同步锁捆绑使用的；而Condition是需要与互斥锁/共享锁捆绑使用的。

    * Condition它更强大的地方在于：能够更加精细的控制多线程的休眠与唤醒。对于同一个锁，我们可以创建多个Condition，在不同的情况下使用不同的Condition。通过Condition，能明确的指定唤醒读线程。

## synchronized与ReentrantLock的比较

1. 锁的实现
   synchronized是JVM实现的，ReentrantLock是JDK实现的。
2. 性能
   新版本 Java 对 synchronized 进行了很多优化，例如自旋锁（就是for循环）等，synchronized 与 ReentrantLock 大致相同。
3. 等待可中断
   当持有锁的线程长期不释放锁的时候，正在等待的线程可以选择放弃等待，改为处理其他事情。ReentrantLock 可中断，而 synchronized 不行。
4. 公平锁
   synchronized 中的锁是非公平的，ReentrantLock 默认情况下也是非公平的，但是也可以是公平的。
5. 锁绑定多个条件
   一个 ReentrantLock 可以同时绑定多个 Condition 对象

## 使用比较

除非使用ReentrantLock的高级功能，否则优先使用synchronized。这是因为 synchronized 是 JVM 实现的一种锁机制，JVM 原生地支持它，而 ReentrantLock 不是所有的 JDK 版本都支持。并且使用 synchronized 不用担心没有释放锁而导致死锁问题，因为 JVM 会确保锁的释放。

# 六、线程之间的协作

多个线程执行任务时，某些线程必须在其他线程执行完成后才能执行，这时候就需要对线程进行协调。

## join()方法

在线程中调用另一个线程的join()方法,会将当前线程挂起，而不是忙等待，直到目标线程执行结束。

## wait(),notify(),notifyAll()

调用 wait() 使得线程等待某个条件满足，线程在等待时会被挂起，当其他线程的运行使得这个条件满足时，其它线程会调用 notify() 或者 notifyAll() 来唤醒挂起的线程。

它们都属于 Object 的一部分，而不属于 Thread。只能用在同步方法或者同步控制块中使用，否则会在运行时抛出 IllegalMonitorStateException。

使用 wait() 挂起期间，线程会释放锁。这是因为，如果没有释放锁，那么其它线程就无法进入对象的同步方法或者同步控制块中，那么就无法执行 notify() 或者 notifyAll() 来唤醒挂起的线程，造成死锁。

### wait()和sleep()的区别

* wait是Object方法，sleep是Thread的静态方法。
* wait会释放锁，sleep不会。

## await(),signal(),signalAll()

java.util.concurrent 类库中提供了 Condition 类来实现线程之间的协调，可以在 Condition 上调用 await() 方法使线程等待，其它线程调用 signal() 或 signalAll() 方法唤醒等待的线程。

只能用在lock.lock()同步时使用，否则会在运行时抛出 IllegalMonitorStateException。

相比于 wait() 这种等待方式，await() 可以指定等待的条件，因此更加灵活。

# 七、J.U.C - AQS
java.util.concurrent（J.U.C）大大提高了并发性能，AQS 被认为是 J.U.C 的核心。

## CountDownLatch

用来控制一个线程等待多个线程。维护了一个计数器cnt，每次调用CountDownLatch的实例方法countDown()都会使计数器的值减一，减到0时，那些调用await()方法而等待的线程就会开始执行。
```java
public static void main(String[] args) throws InterruptedException {
    int count = 5;
    CountDownLatch countDownLatch = new CountDownLatch(count);
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    cachedThreadPool.execute(()->{
        try {
            countDownLatch.await();
            System.out.println("end...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });
    cachedThreadPool.execute(()->{
        for (int i = 0; i < count; i++) {
            countDownLatch.countDown();
            System.out.print("count : "+countDownLatch.getCount()+" ");
        }
    });
    cachedThreadPool.shutdown();
}
// 输出 count : 4 count : 3 count : 2 count : 1 count : 0 end...
// 或者输出 count : 4 count : 3 count : 2 count : 1 end...
//         count : 0
```
## CyclicBarrier

用来控制多个线程互相等待，只有当多个线程都到达时，这些线程才会继续执行。

和 CountdownLatch 相似，都是通过维护计数器来实现的。线程执行 await() 方法之后计数器会减 1，并进行等待，直到计数器为 0，所有调用 await() 方法而在等待的线程才能继续执行。

CyclicBarrier 和 CountdownLatch 的一个区别是，CyclicBarrier 的计数器通过调用 reset() 方法可以循环使用，所以它才叫做循环屏障。

CyclicBarrier 有两个构造函数，其中 parties 指示计数器的初始值，barrierAction 在所有线程都到达屏障的时候会执行一次。

```java
private static void test1() {
    int count = 5;
    CyclicBarrier cyclicBarrier = new CyclicBarrier(count);
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    for (int i = 0; i < count; i++) {
        cachedThreadPool.execute(() -> {
            try {
                System.out.print("before ");
                cyclicBarrier.await();
                System.out.print("after ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        });
    }
    cachedThreadPool.shutdown();
}
//输出 before before before before before after after after after after 
```

```java
 private static void test2() {
    int count = 5;
    CyclicBarrier cyclicBarrier = new CyclicBarrier(count,()->{
        System.out.println("ok ");
    });
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    for (int i = 0; i < count; i++) {
        cachedThreadPool.execute(() -> {
            try {
                System.out.print("before ");
                cyclicBarrier.await();
                System.out.print("after ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        });
    }
    cachedThreadPool.shutdown();
}
//输出 before before before before before ok 
//     after after after after after 
```

## Semaphore

Semaphore（信号量），类似于操作系统中信号量，可以指定线程的并发数量，常用来控制对互斥资源访问的线程数。

```java
public static void main(String[] args) {
    int count = 3;
    int num = 15;
    Semaphore semaphore = new Semaphore(count);// 另一个参数是否公平标志
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    for (int i = 0; i < num; i++) {
        cachedThreadPool.execute(()->{
            try {
                semaphore.acquire(1);
                System.out.print(semaphore.availablePermits()+" ");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                semaphore.release(1);
            }
        });
    }
    cachedThreadPool.shutdown();
}
// 输出 1 1 0 0 0 0 0 0 0 0 1 0 0 0 0 会有多种结果
```

### 方法说明

* Semaphore(int permits):构造方法，创建具有给定许可数的计数信号量并设置为非公平信号量。
* Semaphore(int permits,boolean fair):构造方法，当fair等于true时，创建具有给定许可数的计数信号量并设置为公平信号量。
* void acquire():从此信号量获取一个许可前线程将一直阻塞。相当于一辆车占了一个车位。
* void acquire(int n):从此信号量获取给定数目许可，在提供这些许可前一直将线程阻塞。比如n=2，就相当于一辆车占了两个车位。
* void release():释放一个许可，将其返回给信号量。就如同车开走返回一个车位。
* void release(int n):释放n个许可。
* int availablePermits()：当前可用的许可数。

# J.U.C-其他组件

## FutureTask

## BlockingQueue

[参考](https://www.cnblogs.com/WangHaiMing/p/8798709.html)

### 1.概括
java.util.concurrent.BlockingQueue 接口有以下阻塞队列的实现：
![BlockingQueue](../../../youdaonote-images/3F24FAE079A54454AF90B3714A76A688.jpeg)
* FIFO队列：LinkedBlockingQueue、ArrayBlockingQueue（固定长度）
* 优先级队列：PriorityBlockingQueue

提供了阻塞的 take() 和 put() 方法：如果队列为空 take() 将阻塞，直到队列中有内容；如果队列为满 put() 将阻塞，直到队列有空闲位置。

### 2.主要方法

* boolean add(E e); 增加一个元索，如果设置成功返回true, 否则返回false。
* boolean offer(E e); 添加一个元素，如果设置成功返回true, 否则返回false. e的值不能为空，否则抛出空指针异常。
* void put(E e);  添加一个元素，如果队列满，则**阻塞**。
* offer(E e, long timeout, TimeUnit unit); 在设定时间内添加一个元素并返回true，如果队列已满，则返回false
* E take(); 移除并返回队列头部的元素，如果队列为空，则**阻塞**。
* E poll(long timeout, TimeUnit unit);在给定的时间里，移除并返问队列头部的元6素，时间到了直接调用普通的poll方法，为null则直接返回null。
* int remainingCapacity(); 获取队列中剩余的空间。
* boolean remove(Object o); 从队列中移除指定的值。
* int drainTo(Collection<? super E> c); 将队列中值，全部移除，并发设置到给定的集合中。
* int drainTo(Collection<? super E> c, int maxElements);指定数量限制将队列中值，全部移除，并发设置到给定的集合中。
  
### ArrayBlockingQueue

基于数组的阻塞队列实现，初始化必须指定大小。ArrayBlockingQueue在生产者放入数据和消费者获取数据，都是共用同一个锁对象，由此也意味着两者无法真正并行运行，这点尤其不同于LinkedBlockingQueue；在创建ArrayBlockingQueue时，可以控制对象的内部锁是否采用公平锁，默认采用非公平锁。

```java
public class ArrayBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {
    final Object[] items;
    int takeIndex;//队列尾部在数组的位置
    int putIndex;//队列头部在数组的位置
    int count;//队列元素长度
    final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.items = new Object[capacity];
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
    }
    //...
}
```

### LinkedBlockingQueue

基于链表实现的阻塞队列，如果构造一个LinkedBlockingQueue对象，而没有指定其容量大小，LinkedBlockingQueue会默认一个类似无限大小的容量（Integer.MAX_VALUE），这样的话，如果生产者的速度一旦大于消费者的速度，也许还没有等到队列满阻塞产生，系统内存就有可能已被消耗殆尽了。

### DelayQueue

DelayQueue中的元素只有当其指定的延迟时间到了，才能够从队列中获取到该元素。DelayQueue是一个没有大小限制的队列，因此往队列中插入数据的操作（生产者）永远不会被阻塞，而只有获取数据的操作（消费者）才会被阻塞。

使用场景：
* 缓存系统的设计：可以用DelayQueue保存缓存元素的有效期，使用一个线程循环查询DelayQueue，一旦能从DelayQueue中获取元素时，表示缓存有效期到了。
* 定时任务调度：使用DelayQueue保存当天将会执行的任务和执行时间，一旦从DelayQueue中获取到任务就开始执行。（Timer就是使用DelayQueue实现的）

```java
private final transient ReentrantLock lock = new ReentrantLock();
private final PriorityQueue<E> q = new PriorityQueue<E>();
private Thread leader = null;
private final Condition available = lock.newCondition();
```
以支持优先级的PriorityQueue无界队列作为一个容器，因为元素都必须实现Delayed接口，可以根据元素的过期时间来对元素进行排列，因此，先过期的元素会在队首，每次从队列里取出来都是最先要过期的元素。（不这样排序无法保证最早过期的元素被获取）
**详细使用见代码BlockingQueueTest.java**

### PriorityBlockingQueue

基于优先级的阻塞队列，默认自然排序，也可以自定义实现compareTo()方法来指定元素排序规则，不会阻塞数据生产者，只会在没有可消费的数据时，阻塞数据的消费者。注意当生产者的速度大于消费者时，可能会耗尽堆内存空间。

### SynchronousQueue

 一个不存储元素的阻塞队列，每一个put操作必须等待take操作，否则不能添加元素。支持公平锁和非公平锁。SynchronousQueue的一个使用场景是在线程池里。Executors.newCachedThreadPool()就使用了SynchronousQueue，这个线程池根据需要（新任务到来时）创建新的线程，如果有空闲线程则会重复使用，线程空闲了60秒后会被回收。

### LinkedTransferQueue

一个由链表结构组成的无界阻塞队列，相当于其它队列，LinkedTransferQueue队列多了transfer和tryTransfer方法。

### LinkedBlockingDeque

一个由链表结构组成的双向阻塞队列。队列头部和尾部都可以添加和移除元素，多线程并发时，可以将锁的竞争最多降到一半。

## ForkJoin

是Java7提供的原生多线程并行处理框架，其基本思想是将大任务分割成小任务，最后将小任务聚合起来得到结果。使用工作窃取（work-stealing）算法，主要用于实现“分而治之”。它非常类似于HADOOP提供的MapReduce框架，只是MapReduce的任务可以针对集群内的所有计算节点，可以充分利用集群的能力完成计算任务。ForkJoin更加类似于单机版的MapReduce。

* ForkJoinTask:要使用ForkJoin框架，必须首先创建一个ForkJoin任务。它提供在任务中执行fork()和join的操作机制，通常不直接继承ForkjoinTask类，只需要直接继承其子类。
    * RecursiveAction: 用于没有返回值的任务
    * RecursiveTask: 用于有返回值的任务
* ForkJoinPool：task要通过ForkJoinPool来执行，分割的子任务也会添加到当前工作线程的双端队列中，进入队列的头部。当一个工作线程中没有任务时，会从其他工作线程的队列尾部获取一个任务。线程数量取决于 CPU 核数。

### 工作窃取（work-stealing）算法

ForkJoinPool 实现了工作窃取算法来提高 CPU 的利用率。每个线程都维护了一个双端队列，用来存储需要执行的任务。工作窃取算法允许空闲的线程从其它线程的双端队列中窃取一个任务来执行。窃取的任务必须是最晚的任务，避免和队列所属线程发生竞争。

### 使用ForkJoin
```java
static class MyRecursiveTask extends RecursiveTask<Long>{

    public static final int threshold = 10000;
    private Long start;
    private Long end;

    @Override
    protected Long compute() {
        Long length = end - start;
        if(length < threshold){
            Long sum = 0L;
            for (Long i = start;i<=end;i++){
                sum += i;
            }
            return sum;
        }else {
            Long middle = (end + start)/2;//计算的两个值的中间值
            MyRecursiveTask startTask = new MyRecursiveTask(start,middle);
            MyRecursiveTask endTask = new MyRecursiveTask(middle+1,end);
            startTask.fork();
            endTask.fork();
            return startTask.join()+endTask.join();
        }
    }

    public MyRecursiveTask(Long start, Long end) {
        this.start = start;
        this.end = end;
    }
}

public static void test1(){
    Long start = System.currentTimeMillis();
    MyRecursiveTask myRecursiveTask = new MyRecursiveTask(0L,100000000L);
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    ForkJoinTask<Long> submit = forkJoinPool.submit(myRecursiveTask);
    //Long invoke = forkJoinPool.invoke(myRecursiveTask);
    Long aLong = null;
    try {
        aLong = submit.get();
    } catch (Exception e) {
        e.printStackTrace();
    }
    Long end = System.currentTimeMillis();
    System.out.println("test1 = " + aLong+"  time: " + (end - start));
}
```
注意：
ForkJoinPool 使用submit 或 invoke 提交的区别：invoke是同步执行，调用之后需要等待任务完成，才能执行后面的代码；submit是异步执行，只有在Future调用get的时候会阻塞。

# 十、Java内存模型

Java 内存模型试图屏蔽各种硬件和操作系统的内存访问差异，以实现让 Java 程序在各种平台下都能达到一致的内存访问效果。

## 主内存与工作内存

处理器上的寄存器的读写的速度比内存快几个数量级，为了解决这种速度矛盾，在它们之间加入了高速缓存。
![内存模型](../../../youdaonote-images/338143C994F2409484C0946B9DD30A86.png)
所有的变量都存储在主内存中，每个线程还有自己的工作内存，工作内存存储在高速缓存或者寄存器中，保存了该线程使用的变量的主内存副本拷贝。

线程只能直接操作工作内存中的变量，不同线程之间的变量值传递需要通过主内存来完成。

缓存一致性问题：多个缓存共享同一块主内存区域，那么多个缓存的数据可能会不一致。

## 内存间的交互操作

Java内存模型定义了8个操作完成主内存和工作内存的交互。
![内存间的交互操作](../../../youdaonote-images/713E11ECE9D846679A34EDFA897A5448.jpeg)
* read：把一个变量值从主内存传输到工作内存中。
* load：在read之后执行，把read获取的值放到工作内存变量副本中。
* use：把工作内存中一个变量的值传递给执行引擎。
* assign：把一个从执行引擎接收到的值赋给工作内存的变量。
* store：把工作内存的值传送到主内存中。
* write：在store之后，把store得到的值放入主内存变量中。
* lock：作用于主内存的变量
* unlock

## 内存模型三大特性

### 1.原子性
Java 内存模型保证了 read、load、use、assign、store、write、lock 和 unlock 单独操作具有原子性。但没有被 volatile 修饰的变量load、store、read 和 write 操作可以不具备原子性。
解决方法：
1.使用原子类，如AtomicInteger 
2.使用 synchronized 互斥锁来保证操作的原子性
3.使用lock 和 unlock

### 2.可见性

可见性是指当一个线程修改了变量值时，其他线程能够立即得知这个修改。Java 内存模型是通过在变量修改后将新值同步回主内存，在变量读取前从主内存刷新变量值来实现可见性的。

主要有三种实现可见性的方式：
* volatile（不能保证原子性）
* synchronized，对一个变量执行 unlock 操作之前，必须把变量值同步回主内存。
* final，被 final 关键字修饰的字段在构造器中一旦初始化完成，并且没有发生 this 逃逸（其它线程通过 this 引用访问到初始化了一半的对象），那么其它线程就能看见 final 字段的值。

### 3.有序性
有序性是指：在本线程内观察，所有操作都是有序的。在一个线程观察另一个线程，所有操作都是无序的，无序是因为发生了指令重排序。在 Java 内存模型中，允许编译器和处理器对指令进行重排序，重排序过程不会影响到单线程程序的执行，却会影响到多线程并发执行的正确性。

volatile 关键字通过添加内存屏障的方式来禁止指令重排，即重排序时不能把后面的指令放到内存屏障之前。

也可以通过 synchronized 来保证有序性，它保证每个时刻只有一个线程执行同步代码，相当于是让线程顺序执行同步代码。

## 先行发生原则

JVM规定的先行发生原则，让一个操作无需控制就可以先于另一个操作发生。

* 单一线程原则(Single Thread rule)：在一个线程内，在程序前面的操作先行发生于后面的操作。
* 管程锁定规则(Monitor Lock Rule)：一个 unlock 操作先行发生于后面对同一个锁的 lock 操作。
* volatile变量规则(Volatile Variable Rule)：对一个 volatile 变量的写操作先行发生于后面对这个变量的读操作。
* 线程启动规则(Thread Start Rule)：Thread 对象的 start() 方法调用先行发生于此线程的每一个动作。
* 线程加入规则(Thread Join Rule)：Thread 对象的结束先行发生于 join() 方法返回。
* 线程中断规则(Thread Interruption Rule)：对线程 interrupt() 方法的调用先行发生于被中断线程的代码检测到中断事件的发生，可以通过 interrupted() 方法检测到是否有中断发生。
* 对象终结规则()：一个对象的初始化完成（构造函数执行结束）先行发生于它的 finalize(Finalizer Rule)： 方法的开始。
* 传递性(Transitivity)：如果操作 A 先行发生于操作 B，操作 B 先行发生于操作 C，那么操作 A 先行发生于操作 C。

# 十一、线程安全

多个线程不管以何种方式访问某个类，并且在主调代码中不需要进行同步，都能表现正确的行为。

## 不可变

不可变（Immutable）的对象一定是线程安全的，不需要再采取任何的线程安全保障措施。
不可变类型：
* final 关键字修饰的基本数据类型
* String
* 枚举类型
* Number 部分子类，如 Long 和 Double 等数值包装类型，BigInteger 和 BigDecimal 等大数据类型。但同为 Number 的原子类 AtomicInteger 和 AtomicLong 则是可变的。
* 对于集合类型，可以使用 Collections.unmodifiableXXX() 方法来获取一个不可变的集合。
```java
Map<String, Integer> map = new HashMap<>();
Map<String, Integer> unmodifiableMap = Collections.unmodifiableMap(map);
unmodifiableMap.put("a", 1);//会抛出异常java.lang.UnsupportedOperationException
```
## 互斥同步

synchronized和ReentrantLock

## 非阻塞同步

互斥同步属于阻塞同步，线程阻塞和唤醒会带来性能问题。

### 1.CAS

硬件支持的原子性操作最典型的是：比较并交换（Compare-and-Swap，CAS）。

### 2.AtomicInteger

J.U.C 包里面的整数原子类 AtomicInteger 的方法调用了 Unsafe 类的 CAS 操作。

### 3.ABA问题

J.U.C 包提供了一个带有标记的原子引用类 AtomicStampedReference 来解决这个问题，它可以通过控制变量值的版本来保证 CAS 的正确性。大部分情况下 ABA 问题不会影响程序并发的正确性，如果需要解决 ABA 问题，改用传统的互斥同步可能会比原子类更高效。

## 无同步方案
要保证线程安全，并不是一定就要进行同步。如果一个方法本来就不涉及共享数据，那它自然就无须任何同步措施去保证正确性。

### 1.栈封闭
多个线程访问同一个方法的局部变量时，不会出现线程安全问题

### 2.线程本地存储（Thread Local Storage）
使用 java.lang.ThreadLocal 类来实现线程本地存储功能
```java
ThreadLocal threadLocal = new ThreadLocal();
Thread thread1 = new Thread(() -> {
    threadLocal.set(1);
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.out.println(threadLocal.get());
    threadLocal.remove();
});
Thread thread2 = new Thread(() -> {
    threadLocal.set(2);
    threadLocal.remove();
});
thread1.start();
thread2.start();
//输出 1
```
每个 Thread 都有一个 ThreadLocal.ThreadLocalMap 对象。当调用一个 ThreadLocal 的 set(T value) 方法时，先得到当前线程的 ThreadLocalMap 对象，然后将 ThreadLocal->value 键值对插入到该 Map 中。
ThreadLockl的set方法
```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}
```
get()方法类似
```java
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}
```
### 3.可重入代码(Reentry code)

可重入代码(Reentry code)也叫纯代码(Pure code)是一种允许多个进程同时访问的代码。为了使各进程所执行的代码完全相同，故不允许任何进程对其进行修改。程序在运行过程中可以被打断，并由开始处再次执行，并且在合理的范围内（多次重入，而不造成堆栈溢出等其他问题），程序可以在被打断处继续执行，且执行结果不受影响。

# 十二、锁优化

这里的锁优化主要是指 JVM 对 synchronized 的优化。

## 自旋锁

自旋锁的思想是让一个线程在请求一个共享数据的锁时执行忙循环（自旋）一段时间，如果在这段时间内能获得锁，就可以避免进入阻塞状态。在 JDK 1.6 中引入了自适应的自旋锁。自适应意味着自旋的次数不再固定了，而是由前一次在同一个锁上的自旋次数及锁的拥有者的状态来决定。

## 锁消除

锁消除是指对于被检测出不可能存在竞争的共享数据的锁进行消除。

## 锁粗化

如果虚拟机探测到由有一串零碎的操作都对同一个对象加锁，将会把加锁的范围扩展（粗化）到整个操作序列的外部。

## 轻量级锁

JDK 1.6 引入了偏向锁和轻量级锁，从而让锁拥有了四个状态：无锁状态（unlocked）、偏向锁状态（biasble）、轻量级锁状态（lightweight locked）和重量级锁状态（inflated）。

## 偏向锁

偏向锁的思想是偏向于让第一个获取锁对象的线程，这个线程在之后获取该锁就不再需要进行同步操作，甚至连 CAS 操作也不再需要。

# 十三、多线程开发良好的实践

* 给线程起个有意义的名字，这样可以方便找 Bug。
缩小同步范围，从而减少锁争用。例如对于 synchronized，应该尽量使用同步块而不是同步方法。

* 多用同步工具少用 wait() 和 notify()。首先，CountDownLatch, CyclicBarrier, Semaphore 和 Exchanger 这些同步类简化了编码操作，而用 wait() 和 notify() 很难实现复杂控制流；其次，这些同步类是由最好的企业编写和维护，在后续的 JDK 中还会不断优化和完善。
* 使用 BlockingQueue 实现生产者消费者问题。
多用并发集合少用同步集合，例如应该使用 ConcurrentHashMap 而不是 Hashtable。

* 使用本地变量和不可变类来保证线程安全。

* 使用线程池而不是直接创建线程，这是因为创建线程代价很高，线程池可以有效地利用有限的线程来启动任务。

# 参考资料

- [Java并发](https://github.com/CyC2018/CS-Notes/blob/master/docs/notes/Java%20%E5%B9%B6%E5%8F%91.md)
