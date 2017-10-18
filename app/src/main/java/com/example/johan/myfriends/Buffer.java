package com.example.johan.myfriends;

import java.util.LinkedList;

/**
 * Created by johan on 2017-10-10.
 */

public class Buffer<T>
{
    private LinkedList<T> buffer = new LinkedList<T>();

    public synchronized void put(T element) {
        buffer.addLast(element);
        notifyAll();
    }

    public synchronized T get() throws InterruptedException {
        while(buffer.isEmpty()) {
            wait();
        }
        return buffer.removeFirst();
    }
}
