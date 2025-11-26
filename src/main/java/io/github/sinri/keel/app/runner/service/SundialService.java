package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.core.servant.sundial.KeelSundial;
import io.github.sinri.keel.core.servant.sundial.KeelSundialPlan;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class SundialService extends KeelSundial implements Service {

    @NotNull
    private final Application application;

    public SundialService(@NotNull Application application) {
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

    @Override
    abstract protected Future<Collection<KeelSundialPlan>> fetchPlans();
}
