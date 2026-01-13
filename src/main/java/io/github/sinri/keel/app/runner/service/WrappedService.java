package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
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
class WrappedService extends KeelVerticleBase implements Service {
    private final LateObject<Application> lateApplication = new LateObject<>();

    private final Function<Service, Future<Void>> anything;

    public WrappedService(Function<Service, Future<Void>> anything) {
        super();
        this.anything = anything;
    }

    @Override
    public Application getApplication() {
        return lateApplication.get();
    }

    @Override
    protected Future<Void> startVerticle() {
        return anything.apply(this)
                       .onComplete(ar -> {
                           getVertx().setTimer(100, id -> {
                               this.undeployMe();
                           });
                       });
    }

    @Override
    public Logger getStdoutLogger() {
        return getApplication().getStdoutLogger();
    }

    @Override
    public final Future<String> deployMe(Application application) {
        lateApplication.set(application);
        return deployMe(application.getVertx(), new DeploymentOptions());
    }
}
