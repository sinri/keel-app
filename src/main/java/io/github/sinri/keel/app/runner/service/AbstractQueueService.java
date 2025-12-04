package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.core.servant.queue.QueueDispatcher;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import org.jetbrains.annotations.NotNull;

/**
 * 队列服务
 *
 * @since 5.0.0
 */
public abstract class AbstractQueueService extends QueueDispatcher implements Service {
    @NotNull
    private final Application application;

    public AbstractQueueService(@NotNull Application application) {
        super(application);
        this.application = application;
    }

    @Override
    public @NotNull
    final Application getApplication() {
        return application;
    }

    @Override
    public @NotNull
    final LoggerFactory getLoggerFactory() {
        return application.getLoggerFactory();
    }
}
