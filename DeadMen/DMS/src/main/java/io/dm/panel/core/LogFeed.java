package io.dm.panel.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * The single merged, bounded, thread-safe feed of everything the operator should see
 * in the Server Panel's live console.
 *
 * <p>Three producers push into here (see {@link ConsoleTeeStream} and
 * {@link PanelLogAppender}):
 * <ul>
 *   <li>tee'd {@code System.out} ({@code source="stdout"})</li>
 *   <li>tee'd {@code System.err} ({@code source="stderr"})</li>
 *   <li>a Logback appender attached to the named loggers
 *       {@code logger}/{@code rollingWarnFileLogger}/{@code rollingErrorFileLogger}</li>
 * </ul>
 *
 * <p>Backed by a {@link ConcurrentLinkedDeque} capped at {@link #MAX_LINES}
 * (oldest evicted on overflow) so a long-running server never grows this
 * unbounded. UI tabs subscribe for live-append rather than re-diffing the buffer.
 */
public final class LogFeed {

    /** Bounded ring-buffer capacity. Keeps memory flat on a long-lived server. */
    public static final int MAX_LINES = 5000;

    public static final LogFeed INSTANCE = new LogFeed();

    /** A single console line. {@code level} is INFO/WARN/ERROR/STDOUT/STDERR. */
    public record LogLine(long timestampMs, String level, String source, String message) {}

    private final ConcurrentLinkedDeque<LogLine> lines = new ConcurrentLinkedDeque<>();
    private final AtomicInteger size = new AtomicInteger();
    private final List<Consumer<LogLine>> listeners = new CopyOnWriteArrayList<>();

    private LogFeed() {}

    /** Append a line, evict-oldest if over capacity, then notify subscribers. */
    public void append(LogLine line) {
        if (line == null) {
            return;
        }
        lines.addLast(line);
        if (size.incrementAndGet() > MAX_LINES) {
            if (lines.pollFirst() != null) {
                size.decrementAndGet();
            }
        }
        // Listener callbacks must never throw back into a producer (which could be
        // the game thread writing to System.out). Swallow + best-effort.
        for (Consumer<LogLine> listener : listeners) {
            try {
                listener.accept(line);
            } catch (Throwable ignored) {
                // A misbehaving UI listener must not corrupt logging.
            }
        }
    }

    /** Convenience for producers that only have raw text. */
    public void append(String level, String source, String message) {
        append(new LogLine(System.currentTimeMillis(), level, source, message));
    }

    /** Immutable copy of the current buffer, for initial tab population. */
    public List<LogLine> snapshot() {
        return new ArrayList<>(lines);
    }

    public void subscribe(Consumer<LogLine> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(Consumer<LogLine> listener) {
        listeners.remove(listener);
    }
}
