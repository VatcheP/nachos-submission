package nachos.threads;

import nachos.machine.*;
import java.util.HashMap;

public class Rendezvous {
    private Lock lock;
    private HashMap<Integer, Slot> slots;

    private static class Slot {
        Condition2 cv;
        boolean hasWaiter = false;
        boolean completed = false;
        int waitingValue;
        int responseValue;

        Slot(Lock lock) {
            cv = new Condition2(lock);
        }
    }

    public Rendezvous() {
        lock = new Lock();
        slots = new HashMap<Integer, Slot>();
    }

    public int exchange(int tag, int value) {
        lock.acquire();

        Integer key = Integer.valueOf(tag);
        Slot slot = slots.get(key);

        if (slot == null) {
            slot = new Slot(lock);
            slots.put(key, slot);
        }

        if (!slot.hasWaiter) {
            slot.hasWaiter = true;
            slot.waitingValue = value;
            slot.completed = false;

            while (!slot.completed) {
                slot.cv.sleep();
            }

            int ret = slot.responseValue;

            slot.hasWaiter = false;
            slot.completed = false;
            slot.cv.wake();

            lock.release();
            return ret;
        } else {
            int ret = slot.waitingValue;

            slot.responseValue = value;
            slot.completed = true;
            slot.cv.wake();

            while (slot.hasWaiter) {
                slot.cv.sleep();
            }

            lock.release();
            return ret;
        }
    }

    public static void rendezTest1() {
        final Rendezvous r = new Rendezvous();

        KThread t1 = new KThread(new Runnable() {
            public void run() {
                int tag = 0;
                int send = -1;

                System.out.println("Thread " + KThread.currentThread().getName()
                    + " exchanging " + send);

                int recv = r.exchange(tag, send);

                Lib.assertTrue(recv == 1,
                    "Was expecting " + 1 + " but received " + recv);

                System.out.println("Thread " + KThread.currentThread().getName()
                    + " received " + recv);
            }
        });

        t1.setName("t1");

        KThread t2 = new KThread(new Runnable() {
            public void run() {
                int tag = 0;
                int send = 1;

                System.out.println("Thread " + KThread.currentThread().getName()
                    + " exchanging " + send);

                int recv = r.exchange(tag, send);

                Lib.assertTrue(recv == -1,
                    "Was expecting " + -1 + " but received " + recv);

                System.out.println("Thread " + KThread.currentThread().getName()
                    + " received " + recv);
            }
        });

        t2.setName("t2");

        t1.fork();
        t2.fork();

        t1.join();
        t2.join();
    }

    public static void selfTest() {
        rendezTest1();
    }
}
