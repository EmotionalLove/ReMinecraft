package com.sasha.reminecraft.util;

import java.util.concurrent.CountDownLatch;

public abstract class AwaitThread extends Thread {

    private CountDownLatch latch;

    public AwaitThread(CountDownLatch latch) {
        this.latch = latch;
    }

    public final void finish() {
        latch.countDown();
    }
}
