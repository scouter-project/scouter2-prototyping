package scouter2.common.collection;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 *
 */
public class PurgingQueue<E> {

    private final ArrayBlockingQueue<E> inner;

    public PurgingQueue(int capacity) {
        this.inner = new ArrayBlockingQueue<E>(capacity);
    }

    /**
     * primary function of this class - non blocking put, blocking get
     */
    public boolean offerOverflowClear(E e) {
        boolean added = inner.offer(e);
        if (!added) {
            inner.clear();
        }
        return added;
    }

    /**
     * primary function of this class - non blocking put, blocking get
     */
    public E take() throws InterruptedException {
        return inner.take();
    }
}
