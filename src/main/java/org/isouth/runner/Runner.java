package org.isouth.runner;

public class Runner {

    private Runnable app;

    private Runnable hook;

    private volatile Thread shutdown;

    private volatile Thread awaitThread;

    private volatile boolean stopAwait;

    public Runner withHook(Runnable hook) {
        this.hook = hook;
        return this;
    }

    public Runner run() {
        this.shutdown = new Thread(this::stop);
        Runtime.getRuntime()
                .addShutdownHook(this.shutdown);
        if (this.app != null) {
            this.app.run();
        }
        try {
            awaitThread = Thread.currentThread();
            while (!stopAwait) {
                try {
                    Thread.sleep(10_000L);
                } catch (InterruptedException ex) {
                }
            }
        } finally {
            awaitThread = null;
        }
        if (this.hook != null) {
            this.hook.run();
        }
        return this;
    }

    public Runner run(Runnable app) {
        this.app = app;
        return this.run();
    }

    public Runner start() {
        Thread runner = new Thread(this::run);
        runner.start();
        return this;
    }

    public Runner start(Runnable app) {
        this.app = app;
        return this.start();
    }

    public Runner stop() {
        this.stopAwait = true;
        Thread t = this.shutdown;
        if (t != null) {
            try {
                Runtime.getRuntime()
                        .removeShutdownHook(t);
            } catch (IllegalStateException ex) {
            } finally {
                this.shutdown = null;
            }
        }

        t = awaitThread;
        if (t != null) {
            t.interrupt();
            try {
                t.join(1000);
            } catch (InterruptedException e) {
            }
            awaitThread = null;
        }
        return this;
    }
}
