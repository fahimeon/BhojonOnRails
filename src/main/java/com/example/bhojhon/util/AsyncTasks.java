package com.example.bhojhon.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/**
 * Small helper for running background work off the JavaFX Application Thread
 * using a shared daemon thread pool and {@link Task}.
 *
 * <p>Replaces ad-hoc {@code new Thread(...).start()} usage: success/error
 * callbacks are delivered back on the FX thread automatically, threads are
 * pooled and daemon (so they never block JVM shutdown), and failures are
 * surfaced instead of silently swallowed.
 */
public final class AsyncTasks {

    private static final ExecutorService POOL = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "bhojhon-async");
        thread.setDaemon(true);
        return thread;
    });

    private AsyncTasks() {
    }

    /**
     * Runs {@code work} in the background. {@code onSuccess} is invoked on the
     * FX thread with the result; {@code onError} on the FX thread with the
     * thrown exception.
     *
     * @return the submitted {@link Task} (can be used to cancel).
     */
    public static <T> Task<T> run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        POOL.execute(task);
        return task;
    }

    /** Shuts the pool down; call on application stop. */
    public static void shutdown() {
        POOL.shutdownNow();
    }
}
