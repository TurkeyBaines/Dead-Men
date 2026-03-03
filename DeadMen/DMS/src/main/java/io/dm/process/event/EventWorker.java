package io.dm.process.event;

import io.dm.process.CoreWorker;
import io.dm.process.task.TaskWorker;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EventWorker extends TaskWorker {

    private static final ConcurrentLinkedQueue<Event> EVENTS = new ConcurrentLinkedQueue<>();

    public static void process() {
        EVENTS.removeIf(event -> !event.tick());
    }

    public static Event createEvent(EventConsumer eventConsumer) {
        /* manual processing is required when using this! */
        return new Event(eventConsumer);
    }

    public static Event startEvent(EventConsumer eventConsumer) {
        Event event = new Event(eventConsumer);
        if(!CoreWorker.isPast(CoreWorker.Stage.LOGIC)) {
            if (!event.tick())
                return event;
        }
        EVENTS.add(event);
        return event;
    }

}