package nachos.threads;

import nachos.machine.*;

import java.util.PriorityQueue;
import java.util.Comparator;


class ManagerThread {

    public ManagerThread(KThread t, long time) {
        this.thread = t;
        this.wakingTime = time;
    }

    public KThread getThread() {
        return this.thread;
    }

    public long getWakingTime() {
        return wakingTime;
    }

    public static class Comparer<T> implements Comparator<T> {
        public int compare(T mt1, T mt2) {
            ManagerThread thread1 = (ManagerThread) mt1;
            ManagerThread thread2 = (ManagerThread) mt2;

            if (thread1.getWakingTime() > thread2.getWakingTime())
                return 1;

            else if (thread1.getWakingTime() < thread2.getWakingTime())
                return -1;

            else
                return 0;
        }
    }

    private KThread thread;
    private long wakingTime;

}


/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() {
                timerInterrupt(); }
            });

        managerQueue = new PriorityQueue<ManagerThread>(3, new ManagerThread.Comparer<ManagerThread>());
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        ManagerThread managerThread = managerQueue.peek();

        while ( managerThread != null ) {

            /// machine timer greater than threads waikingUp time
            if (Machine.timer().getTime() >= managerThread.getWakingTime()) {
                Lib.assertTrue(managerThread.getThread() != null);
                Machine.interrupt().disable();

                managerQueue.poll();

                managerThread.getThread().ready();

                managerThread = managerQueue.peek();
            }
            else {
                break;
            }
        }

        /// sends the currents thread at the last of the queue
        KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	    // for now, cheat just to get something working (busy waiting is bad)
        ManagerThread managerThread = new ManagerThread(KThread.currentThread(),
                Machine.timer().getTime() + x);
        managerQueue.add(managerThread);

        boolean InterruptSts = Machine.interrupt().disable();
        KThread.currentThread().sleep();
        Machine.interrupt().restore(InterruptSts);
    }

    private static PriorityQueue<ManagerThread> managerQueue;
}
