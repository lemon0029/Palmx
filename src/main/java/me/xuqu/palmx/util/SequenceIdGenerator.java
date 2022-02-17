package me.xuqu.palmx.util;

import java.util.concurrent.atomic.AtomicInteger;

public class SequenceIdGenerator {

    private final static AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    public static Integer nextId() {
        return ATOMIC_INTEGER.addAndGet(1);
    }
}
