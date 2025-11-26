package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 通过一个异步逻辑快速构建的服务。
 *
 * @since 5.0.0
 */
class WrappedService extends AbstractKeelVerticle implements Service {
    @NotNull
    private final Application application;
    @NotNull
    private final Supplier<Future<Void>> anything;
    private final boolean autoUndeploy;

    public WrappedService(@NotNull Application application, @NotNull Supplier<Future<Void>> anything, boolean autoUndeploy) {
        this.application = application;
        this.anything = anything;
        this.autoUndeploy = autoUndeploy;
    }

    @Override
    public @NotNull Application getApplication() {
        return application;
    }

    @Override
    public Future<String> deployMe() {
        return this.deployMe(new DeploymentOptions());
    }

    @Override
    protected Future<Void> startVerticle() {
        return anything.get()
                       .onComplete(ar -> {
                           if (autoUndeploy) {
                               this.undeployMe();
                           }
                       });
    }
}
