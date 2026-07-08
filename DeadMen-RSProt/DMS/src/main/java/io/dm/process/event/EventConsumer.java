package io.dm.process.event;

@FunctionalInterface
public interface EventConsumer {

    void accept(Event event);

}