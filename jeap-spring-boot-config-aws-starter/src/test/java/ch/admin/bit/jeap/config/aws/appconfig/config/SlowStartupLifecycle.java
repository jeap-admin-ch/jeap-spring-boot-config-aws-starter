package ch.admin.bit.jeap.config.aws.appconfig.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;

/**
 * Simulates a lifecycle component that takes long to start (like an embedded web server), so that AppConfig
 * polls happen after all beans are created but before the application context has finished refreshing.
 */
class SlowStartupLifecycle implements SmartLifecycle {

    /**
     * Runs at the beginning of {@link #start()}, e.g. to deploy a configuration change during startup.
     */
    static volatile Runnable onStart = () -> {};

    @Value("${slowstartupmillis:0}")
    long slowStartupMillis;

    private volatile boolean running;

    @Override
    public void start() {
        onStart.run();
        try {
            Thread.sleep(slowStartupMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
