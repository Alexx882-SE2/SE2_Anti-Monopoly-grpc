package at.aau.anti_mon.client.events;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Test Queue class to handle the event queue
 * --> Problem was that sometimes Events are fired but no EventBus is registered
 */
public class GlobalEventQueue {
    private final Queue<Object> eventQueue = new LinkedList<>();
    private boolean isEventBusReady = false;

    public synchronized void enqueueEvent(Object event) {
        Log.d("ANTI-MONOPOLY-DEBUG", "Enqueue Event: " + event);
        if (isEventBusReady) {
            EventBus.getDefault().post(event);
        } else {
            eventQueue.add(event);
        }
    }

    /**
     * Set the EventBus ready
     * @param ready
     */
    public synchronized void setEventBusReady(boolean ready) {
        Log.d("ANTI-MONOPOLY-DEBUG", "EventBus ready: " + ready);
        isEventBusReady = ready;
        if (ready) {
            flushEvents();
        }
    }

    private void flushEvents() {
        Log.d("ANTI-MONOPOLY-DEBUG", "Flush Events");
        while (!eventQueue.isEmpty()) {
            EventBus.getDefault().post(eventQueue.poll());
        }
    }
}