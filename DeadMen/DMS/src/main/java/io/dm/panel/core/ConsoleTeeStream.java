package io.dm.panel.core;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * A {@link PrintStream} that mirrors everything written to {@code System.out} /
 * {@code System.err} into the panel's {@link LogFeed} <em>and</em> still forwards it
 * to the original underlying stream (so the terminal / any {@code > out.log}
 * redirection keeps working unchanged).
 *
 * <p>This is essential because a large amount of this codebase's debug output is raw
 * {@code System.out.println(...)} and {@code Throwable.printStackTrace()} that never
 * touches SLF4J. Teeing the streams is the only way the Console tab can show
 * "everything going on."
 *
 * <p>We buffer bytes until a newline so that {@code printStackTrace()} (which writes
 * many small chunks) and normal {@code println} both surface as clean, whole lines.
 * Thread-safe: writes are synchronized on the line buffer.
 */
public final class ConsoleTeeStream extends PrintStream {

    private final String source;
    private final StringBuilder lineBuffer = new StringBuilder(256);

    private ConsoleTeeStream(PrintStream original, String source) {
        super(original, true, StandardCharsets.UTF_8);
        this.source = source;
    }

    /**
     * Install the tee on both stdout and stderr. Idempotent-safe to call once during
     * panel bootstrap. Existing holders of {@code System.out} that re-fetch it per use
     * (e.g. {@code ServerWrapper.println}) pick this up immediately.
     */
    public static void install() {
        // Guard against double-install (would nest tees and duplicate lines).
        if (System.out instanceof ConsoleTeeStream && System.err instanceof ConsoleTeeStream) {
            return;
        }
        System.setOut(new ConsoleTeeStream(System.out, "stdout"));
        System.setErr(new ConsoleTeeStream(System.err, "stderr"));
    }

    private void record(int b) {
        // Called under lock. Accumulate until newline, then flush a whole line.
        char c = (char) (b & 0xFF);
        if (c == '\n') {
            flushLine();
        } else if (c != '\r') {
            lineBuffer.append(c);
        }
    }

    private void flushLine() {
        if (lineBuffer.length() == 0) {
            return;
        }
        String line = lineBuffer.toString();
        lineBuffer.setLength(0);
        // Never let feed delivery throw back into the writing (possibly game) thread.
        try {
            LogFeed.INSTANCE.append(source.equals("stderr") ? "STDERR" : "STDOUT", source, line);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void write(int b) {
        synchronized (lineBuffer) {
            record(b);
        }
        super.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        synchronized (lineBuffer) {
            for (int i = 0; i < len; i++) {
                record(buf[off + i]);
            }
        }
        super.write(buf, off, len);
    }
}
