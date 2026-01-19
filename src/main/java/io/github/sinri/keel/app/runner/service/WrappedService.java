package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.ProgramContext;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

import java.util.function.Function;

/**
 * 通过一个异步逻辑快速构建的服务。
 * <p>
 * 不要通过这个类来启动需要长期驻留的实例，要考虑 verticle 解除部署时会清退内部实例的设定。
 *
 * @since 5.0.0
 */
@NullMarked
class WrappedService<P extends ProgramContext> extends KeelVerticleBase implements Service<P> {
    private final LateObject<P> lateProgramContext = new LateObject<>();
    private final Logger logger;
    private final Function<Service<P>, Future<Void>> anything;


    public WrappedService(Function<Service<P>, Future<Void>> anything) {
        super();
        this.anything = anything;
        this.logger = StdoutLoggerFactory.getInstance().createLogger(getClass().getName());
    }

    @Override
    public P getProgramContext() {
        return lateProgramContext.get();
    }

    @Override
    public Future<String> deployMe(Vertx vertx, P programContext) {
        lateProgramContext.set(programContext);
        return deployMe(vertx, new DeploymentOptions());
    }

    @Override
    protected Future<Void> startVerticle() {
        return anything.apply(this)
                       .andThen(ar -> {
                           if (ar.failed()) {
                               logger.error("Failed to start wrapped service: " + ar.cause().getMessage());
                               if (this.isIndispensableService()) {
                                   logger.fatal("Indispensable service failed to start, shutting down the application.");
                                   vertx.close();
                               }
                           }
                           getVertx().setTimer(100, id -> {
                               this.undeployMe();
                           });
                       });
    }

}
