package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

public class Condition2 {
    public Condition2(Lock conditionLock) {
        this.conditionLock = conditionLock;
        waitQueue = new LinkedList<KThread>();
    }

    public void sleep() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        boolean intStatus = Machine.interrupt().disable();

        waitQueue.add(KThread.currentThread());

        conditionLock.release();
        KThread.sleep();
        conditionLock.acquire();

        Machine.interrupt().restore(intStatus);
    }

   
    public void wake() {
    Lib.assertTrue(conditionLock.isHeldByCurrentThread());

    boolean intStatus = Machine.interrupt().disable();

    if (!waitQueue.isEmpty()) {
        KThread thread = waitQueue.removeFirst();

        if (!ThreadedKernel.alarm.cancel(thread)) {
            thread.ready();
        }
    }

    Machine.interrupt().restore(intStatus);
    }    

    public void wakeAll() {
    Lib.assertTrue(conditionLock.isHeldByCurrentThread());

    while (!waitQueue.isEmpty()) {
        wake();
    }
    }

    public void sleepFor(long timeout) {
    Lib.assertTrue(conditionLock.isHeldByCurrentThread());

    if (timeout <= 0) {
        return;
    }

    boolean intStatus = Machine.interrupt().disable();

    KThread current = KThread.currentThread();

    waitQueue.add(current);

    conditionLock.release();
    ThreadedKernel.alarm.waitUntil(timeout);
    conditionLock.acquire();

    waitQueue.remove(current);

    Machine.interrupt().restore(intStatus);
    }


    private Lock conditionLock;
    private LinkedList<KThread> waitQueue;

    private static class InterlockTest {
    private static Lock lock;
    private static Condition2 cv;

    private static class Interlocker implements Runnable {
        public void run() {
            lock.acquire();
            for (int i = 0; i < 10; i++) {
                System.out.println(KThread.currentThread().getName());
                cv.wake();
                cv.sleep();
            }
            lock.release();
        }
    }

    public InterlockTest() {
        lock = new Lock();
        cv = new Condition2(lock);

        KThread ping = new KThread(new Interlocker());
        ping.setName("ping");
        KThread pong = new KThread(new Interlocker());
        pong.setName("pong");

        ping.fork();
        pong.fork();

        ping.join();
        }
    }

    public static void selfTest() {
        new InterlockTest();
    }    


}
