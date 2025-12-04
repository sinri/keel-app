package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.core.servant.sundial.Sundial;
import io.github.sinri.keel.core.servant.sundial.SundialPlan;
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
public abstract class AbstractSundialService extends Sundial implements Service {

    @NotNull
    private final Application application;

    public AbstractSundialService(@NotNull Application application) {
        super(application);
        this.application = application;
    }

    @NotNull
    public static AbstractSundialService wrap(@NotNull Application application, @NotNull Supplier<Future<Collection<SundialPlan>>> plansFetcher) {
        return new AbstractSundialService(application) {
            @Override
            protected @NotNull Future<Collection<SundialPlan>> fetchPlans() {
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
