package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.common.AppRecordingMixin;
import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.base.verticles.KeelVerticle;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface Service extends AppRecordingMixin, KeelVerticle {
    static Service wrap(@NotNull Application application, @NotNull Supplier<Future<Void>> anything, boolean autoUndeploy) {
        return new WrappedService(application, anything, autoUndeploy);
    }

    static Service wrap(@NotNull Application application, @NotNull Supplier<Future<Void>> anything) {
        return wrap(application, anything, true);
    }

    @NotNull
    Application getApplication();

    Future<String> deployMe();
}
