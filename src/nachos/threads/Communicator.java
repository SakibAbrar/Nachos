package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speakerCondition and a listenerCondition are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listenerCondition.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listenerCondition should receive <i>word</i>.
     *
     * @param	msg	the integer to transfer.
     */
    public void speak(int msg) {
        // Aquire the lock
        sharedLock.acquire();

        // forcing the speakerCondition to wait until an audience comes.
        while (msgBuffer != null) {
            speakerCondition.sleep();
        }

        msgBuffer = msg;

        listenerCondition.wake();

        returnCondition.sleep();

        // releasing the lock
        sharedLock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        int wordToReturn;

        // Aquire the lock
        sharedLock.acquire();

        //
        while (msgBuffer == null)
        {
            listenerCondition.sleep();
        }

        wordToReturn = msgBuffer.intValue();
        msgBuffer = null;

        speakerCondition.wake();

        returnCondition.wake();

        // release the lock
        sharedLock.release();

        return wordToReturn;
    }

    private Lock sharedLock = new Lock();
    private Condition2 speakerCondition = new Condition2(sharedLock);
    private Condition2 listenerCondition = new Condition2(sharedLock);
    private Condition2 returnCondition = new Condition2(sharedLock);
    private Integer msgBuffer = null;

}
