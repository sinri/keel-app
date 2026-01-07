package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.core.servant.queue.QueueDispatcher;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

/**
 * 队列服务
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class AbstractQueueService extends QueueDispatcher implements Service {

    private final LateObject<Application> lateApplication = new LateObject<>();

    public AbstractQueueService() {
        super();
    }

    @Override
    public final Application getApplication() {
        return lateApplication.get();
    }

    @Override
    public
    final LoggerFactory getLoggerFactory() {
        return getApplication().getLoggerFactory();
    }

    @Override
    public Future<String> deployMe(Application application) {
        lateApplication.set(application);
        return super.deployMe(application.getVertx());
    }
}
