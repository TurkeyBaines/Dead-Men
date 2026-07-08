package io.dm.process.task;

@FunctionalInterface
public interface TaskConsumer {
    void accept(Task0 task);
}