
package nachos.threads;

import nachos.machine.*;
import java.util.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    private static class Waiter implements Comparable<Waiter> {
        long wakeTime;
        KThread thread;

        Waiter(long wakeTime, KThread thread) {
            this.wakeTime = wakeTime;
            this.thread = thread;
        }

        public int compareTo(Waiter other) {
            if (this.wakeTime < other.wakeTime) return -1;
            if (this.wakeTime > other.wakeTime) return 1;
            return 0;
        }
    }

    private PriorityQueue<Waiter> waitQueue = new PriorityQueue<Waiter>();

    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     */
    public Alarm() {
        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() {
                timerInterrupt();
            }
        });
    }

    /**
     * The timer interrupt handler.
     */
    public void timerInterrupt() {
        boolean intStatus = Machine.interrupt().disable();

        long now = Machine.timer().getTime();

        while (!waitQueue.isEmpty() && waitQueue.peek().wakeTime <= now) {
            Waiter w = waitQueue.poll();
            w.thread.ready();
        }

        Machine.interrupt().restore(intStatus);

        KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least x ticks.
     */
    public void waitUntil(long x) {
        if (x <= 0) {
            return;
        }

        boolean intStatus = Machine.interrupt().disable();

        long wakeTime = Machine.timer().getTime() + x;
        waitQueue.add(new Waiter(wakeTime, KThread.currentThread()));

        KThread.sleep();

        Machine.interrupt().restore(intStatus);
    }

    /**
     * Cancel any timer set by thread.
     */
    public boolean cancel(KThread thread) {
    boolean intStatus = Machine.interrupt().disable();

    Waiter target = null;

    for (Waiter w : waitQueue) {
        if (w.thread == thread) {
            target = w;
            break;
        }
    }

    if (target != null) {
        waitQueue.remove(target);
        thread.ready();
        Machine.interrupt().restore(intStatus);
        return true;
    }

    Machine.interrupt().restore(intStatus);
    return false;
    }

    public static void selfTest() {
        alarmTest1();
    }

    public static void alarmTest1() {
        int durations[] = {1000, 10 * 1000, 100 * 1000};
        long t0, t1;

        for (int d : durations) {
            t0 = Machine.timer().getTime();
            ThreadedKernel.alarm.waitUntil(d);
            t1 = Machine.timer().getTime();
            System.out.println("alarmTest1: waited for " + (t1 - t0) + " ticks");
        }
    }
}
