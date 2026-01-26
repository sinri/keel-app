package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.ProgramContext;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.core.servant.queue.QueueDispatcher;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

/**
 * 队列服务
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class AbstractQueueService<P extends ProgramContext> extends QueueDispatcher implements Service<P> {

    private final LateObject<P> lateProgramContext = new LateObject<>();

    public AbstractQueueService() {
        super();
    }

    @Override
    public final P getProgramContext() {
        return lateProgramContext.get();
    }


    @Override
    public Future<String> deployMe(Keel keel, P programContext) {
        lateProgramContext.set(programContext);
        return super.deployMe(keel);
    }
}
