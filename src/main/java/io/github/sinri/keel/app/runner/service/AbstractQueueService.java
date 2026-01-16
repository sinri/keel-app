package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.ProgramContext;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.core.servant.queue.QueueDispatcher;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

/**
 * 队列服务
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class AbstractQueueService<P extends ProgramContext> extends QueueDispatcher implements Service<P> {

    private final LateObject<P> lateProgramContext = new LateObject<>();
    private final Logger logger;

    public AbstractQueueService() {
        super();
        this.logger = StdoutLoggerFactory.getInstance().createLogger(getClass().getName());
    }

    @Override
    public final P getProgramContext() {
        return lateProgramContext.get();
    }

    @Override
    public final Logger getStdoutLogger() {
        return logger;
    }

    @Override
    public Future<String> deployMe(Vertx vertx, P programContext) {
        lateProgramContext.set(programContext);
        return super.deployMe(vertx);
    }
}
