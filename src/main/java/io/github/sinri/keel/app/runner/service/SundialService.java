package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.core.servant.sundial.KeelSundial;
import io.github.sinri.keel.core.servant.sundial.KeelSundialPlan;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 定时任务服务。
 * <p>
 * 定时任务服务，用于执行周期性任务。
 *
 * @since 5.0.0
 */
public abstract class SundialService extends KeelSundial implements Service {

    @NotNull
    private final Application application;

    public SundialService(@NotNull Application application) {
        this.application = application;
    }

    public static SundialService wrap(@NotNull Application application, @NotNull Supplier<Future<Collection<KeelSundialPlan>>> plansFetcher) {
        return new SundialService(application) {
            @Override
            protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                return plansFetcher.get();
            }
        };
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
