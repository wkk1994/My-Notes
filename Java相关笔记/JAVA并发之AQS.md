[toc]

# JAVA并发之AQS

## 简介

AbstractQueuedSynchronizer的简称，是J.U.C（java.util.concurrent）的核心。内部维护了一个volatile int state（代表共享资源）和一个FIFO队列（多线程阻塞队列）。
AQS同时提供互斥模式（exculsive）和共享模式（shared）。

AQS的主要使用方式是继承它作为一个内部辅助类实现同步原理，它可以简化并发工具的内部实现，屏蔽同步状态管理、线程的排队、等待与唤醒等底层操作。

* exclusive: 只有一个线程能同时运行，如ReentrantLock
* share: 多个线程可以同时执行，如CountDownLatch

## 同步器

不同的同步器争用共享资源的方式也不同，自定义同步器在实现时只需要实现共享资源state的获取与释放方式即可，至于具体线程等待队列的维护（如获取资源失败入队/唤醒出队等），AQS已经在顶层实现好了。一般来说同步器要么是独占方式，或者共享方式，只需要实现AQS的tryAcquire-tryRelease、tryAcquireShared-tryReleaseShared中的一种即可。当时也同时支持实现独占和共享两种方式，如ReentrantReadWriteLock。

自定义同步器实现时主要实现以下几种方法：

* isHeldExclusively()：该线程是否正在独占资源。只有用到condition才需要去实现它。
* tryAcquire(int)：独占方式。尝试获取资源，成功返回true，失败返回false。
* tryRelease(int)：独占模式。尝试释放资源，成功则返回true，失败则返回false。
* tryAcquireShared(int)：共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
* tryReleaseShared(int)：共享方式。尝试释放资源，如果释放后允许唤醒后续等待结点返回true，否则返回false。

## state状态

AQS内部维护了一个volatile int类型变量，用于表示当前同步状态，（volatile只能保证内存可见性，不能保证原子性）。state的访问方式：

* getState()
* setState()
* compareAndSetState() 最常用，依赖于Unsafe的compareAndSwapInt

## 方法

|方法|描述|
|--|--|
|void acquire(int arg)|获取独占锁。会调用`tryAcquire`方法，如果未获取成功，则会进入同步队列等待|
|void acquireInterruptibly(int arg)|响应中断版本的`acquire`，会先判断是否中断然后再获取资源，获取资源中出现中断就`throw new InterruptedException()`|
|boolean tryAcquireNanos(int arg,long nanos)|响应中断+超时的`acquire`，会先判断是否中断然后再获取资源，获取过程中超时`return false`，获取资源中出现中断就`throw new InterruptedException()`|
|void acquireShared(int arg)|获取共享锁。会调用`tryAcquireShared`方法|
|void acquireSharedInterruptibly(int arg)|响应中断版本的`acquireShared`，与`acquireInterruptibly(int arg)`类似|
|boolean tryAcquireSharedNanos(int arg,long nanos)|响应中断+超时的`acquireShared`，与`tryAcquireNanos(int arg)`类似|
|boolean release(int arg)|释放独占锁|
|boolean releaseShared(int arg)|释放共享锁|
|Collection getQueuedThreads()|	获取同步队列上的线程集合|

## 源码实现

### acquire(int arg) 

以独占的方式获取资源，获取到资源返回，获取不到线程进入等待队列，直到获取到资源为止。

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

大致流程：
*  调用自定义同步器的tryAcquire()尝试直接去获取资源，如果成功则直接返回；
* 获取不到资源时，addWaiter(Node.EXCLUSIVE), arg)将该线程加入等待队列的尾部，并标记为独占模式；
* acquireQueued()，进入等待队列，当轮到时（unpark）会去尝试获取资源获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。
* 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。

* tryAcquire(arg) 需要同步器自己实现

```java
protected boolean tryAcquire(int arg) {
    throw new UnsupportedOperationException();
}
```

* addWaiter(Node)

将当前线程加入到等待队列的队尾，并返回当前线程所在的结点。

```java
private Node addWaiter(Node mode) {
    //以给定模式创建一个node节点
    Node node = new Node(Thread.currentThread(), mode);
    //尝试直接放到队尾
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    //直接放到队尾失败，通过enq放入队列
    enq(node);
    return node;
}
```

node的几个状态：

* `CANCELLED`：值为1，在同步队列中等待的线程等待超时或被中断，需要从同步队列中取消该Node的结点，其结点的waitStatus为CANCELLED，即结束状态，进入该状态后的结点将不会再变化。

* `SIGNAL`：值为-1，被标识为该等待唤醒状态的后继结点，当其前置结点的线程释放了同步锁或被取消，将会通知该后继结点的线程执行。这个状态一般都是后继线程来设置前驱节点的。

* `CONDITION`：值为-2，与Condition相关，该标识的结点处于等待队列中，结点的线程等待在Condition上，当其他线程调用了Condition的signal()方法后，CONDITION状态的结点将从等待队列转移到同步队列中，等待获取同步锁。

* `PROPAGATE`：值为-3，与共享模式相关，在共享模式中，该状态标识结点的线程处于可运行状态。

* `0`：值为0，代表初始化状态。

AQS在判断状态时，通过用waitStatus>0表示取消状态，而waitStatus<0表示有效状态。

* end(node)

```java
private Node enq(final Node node) {
    // 自旋直到成功进入队列
    for (;;) {
        Node t = tail;
        if (t == null) { // tail为空初始化一个
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}

```

* acquireQueued(Node, int)

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;// 标记是否成功拿到资源
    try {
        boolean interrupted = false;// 中断状态
        for (;;) {
            final Node p = node.predecessor();// 当前节点的前置节点
            if (p == head && tryAcquire(arg)) {// 当前置节点为head时，当前节点有资格获取资源
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;//返回等待过程中是否被中断过
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
                //如果等待过程中被中断过，哪怕只有那么一次，就将interrupted标记为true
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

shouldParkAfterFailedAcquire(p, node)： 用于检查当前节点的前置节点是否处于SIGNAL状态，只有处于SIGNAL状态，当前节点才可以进入wait

```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;//拿到前驱的状态
    if (ws == Node.SIGNAL)
        //如果已经告诉前驱拿完号后通知自己一下，那就可以安心休息了
        return true;
    if (ws > 0) {
        /*
         * 如果前驱放弃了，那就一直往前找，直到找到最近一个正常等待的状态，并排在它的后边。
         * 注意：那些放弃的结点，由于被自己“加塞”到它们前边，它们相当于形成一个无引用链，稍后就会被GC回收
         */
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
         /**
          * 等待状态为0或者PROPAGATE(-3)，设置前驱的等待状态为SIGNAL,
          * 并且之后会回到循环再次重试获取锁。
          */
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
```

parkAndCheckInterrupt()：将现场park并且返回中断状态

```java
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);//调用park()使线程进入waiting状态
    return Thread.interrupted();//如果被唤醒，查看自己是不是被中断的。
}
```

* cancelAcquire(node)

    该方法实现某个node取消获取锁。

    ```java
    private void cancelAcquire(Node node) {
        if (node == null)
            return;

        node.thread = null;

        // 获取状态小于0的前置节点
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;

        // predNext is the apparent node to unsplice. CASes below will
        // fail if not, in which case, we lost race vs another cancel
        // or signal, so no further action is necessary.
        Node predNext = pred.next;

        // Can use unconditional write instead of CAS here.
        // After this atomic step, other Nodes can skip past us.
        // Before, we are free of interference from other threads.
        node.waitStatus = Node.CANCELLED;

        // 如果当前节点是tail，设置tail = pred
        if (node == tail && compareAndSetTail(node, pred)) {
            // 设置pred的下一个节点为null，方便GC，失败也可以
            compareAndSetNext(pred, predNext, null);
        } else {
            // 如果当前节点不是tail或者设置tail失败，就需要将当前节点的next节点设置到SIGNAL的节点上
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                /*
                * 这时说明pred == head或者pred状态取消或者pred.thread == null
                * 在这些情况下为了保证队列的活跃性，需要去唤醒一次后继线程。
                */
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }
    ```
### release(int arg))

释放指定量的资源，前提是已经获取到资源，所以可以直接setState。当state=0时彻底释放，当释放资源时会unpark等待队列里的线程。

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {// 尝试释放资源
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

* tryRelease(arg)

    需要同步器自己实现。

* unparkSuccessor(Node node)

    唤醒等待队列的下一个线程

```java
private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;// 获取当前节点的状态
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);// 置零当前节点的状态 允许失败
    Node s = node.next;// 获取当前节点的下一个节点
    if (s == null || s.waitStatus > 0) {
        // 如果下一个节点的为null或者状态 >0 继续获取其他节点
        // 从tail（最后一个节点）向前获取最接近head的未取消node
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);// 唤醒线程
}
```

### acquireShared(int arg)

共享模式下获取资源，获取指定量的资源，获取成功直接返回，获取失败进入等待队列，直到获取到资源为止。忽略中断。当获取资源后剩余资源大于0唤醒下一个等待线程。

```java
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        doAcquireShared(arg);
}
```

* tryAcquireShared(arg)

    需要同步器自己实现。尝试获取资源，返回负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。

* doAcquireShared(arg)

    获取资源失败时，将当前线程加入等待队列尾部休息，等待其他线程释放资源时唤醒它。

    ```java
    private void doAcquireShared(int arg) {
        // 将线程加入队尾
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    // 尝试获取资源
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        // 将head指向自己，还有剩余资源唤醒其他等待线程
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                // 判断状态，寻找安全点，进入waiting状态，等着被unpark()或interrupt()
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
    ```

* setHeadAndPropagate(node, r);

    ```java
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // Record old head for check below
        // 设置head
        setHead(node);
        // 如果剩余量大于0唤醒下一个线程
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }
    ```

###  releaseShared(int arg)

共享模式下资源释放，释放成功返回true，并且唤醒等待线程。

```java
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        doReleaseShared();
        return true;
    }
    return false;
}
```

* tryReleaseShared(arg)

    需要同步器自己实现，尝试释放资源，释放成功返回true

* doReleaseShared()

    唤醒后继线程

    ```java

    private void doReleaseShared() {
         /*
     * 以下的循环做的事情就是，在队列存在后继线程的情况下，唤醒后继线程；
     * 或者由于多线程同时释放共享锁由于处在中间过程，读到head节点等待状态为0的情况下，
     * 虽然不能unparkSuccessor，但为了保证唤醒能够正确稳固传递下去，设置节点状态为PROPAGATE。
     * 这样的话获取锁的线程在执行setHeadAndPropagate时可以读到PROPAGATE，从而由获取锁的线程去释放后继等待线程。
     */
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                // 如果队列中存在后继线程。
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                }
                // 如果h节点的状态为0，需要设置为PROPAGATE用以保证唤醒的传播。
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            // 检查h是否仍然是head，如果不是的话需要再进行循环。
            if (h == head)                   // loop if head changed
                break;
        }
    }
    ```

## 应用

* `ReentrantLock`: 使用了AQS的独占获取和释放,用state变量记录某个线程获取独占锁的次数,获取锁时+1，释放锁时-1，在获取时会校验线程是否可以获取锁。
* `Semaphore`: 使用了AQS的共享获取和释放，用state变量作为计数器，只有在大于0时允许线程进入。获取锁时-1，释放锁时+1。
* `CountDownLatch`: 使用了AQS的共享获取和释放，用state变量作为计数器，在初始化时指定。只要state还大于0，获取共享锁会因为失败而阻塞，直到计数器的值为0时，共享锁才允许获取，所有等待线程会被逐一唤醒。

## 参考

* [AbstractQueuedSynchronizer源码解读](https://www.cnblogs.com/micrari/p/6937995.html)
* [Java并发之AQS详解](https://www.cnblogs.com/waterystone/p/4920797.html)