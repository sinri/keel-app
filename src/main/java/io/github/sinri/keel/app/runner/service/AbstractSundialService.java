package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.core.servant.sundial.Sundial;
import io.github.sinri.keel.core.servant.sundial.SundialPlan;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 定时任务服务。
 * <p>
 * 定时任务服务，用于执行周期性任务。
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class AbstractSundialService extends Sundial implements Service {

    private final LateObject<Application> lateApplication = new LateObject<>();

    public AbstractSundialService() {
        super();
    }

    public static AbstractSundialService wrap(Supplier<Future<@Nullable Collection<SundialPlan>>> plansFetcher) {
        return new AbstractSundialService() {
            @Override
            protected Future<@Nullable Collection<SundialPlan>> fetchPlans() {
                return plansFetcher.get();
            }

            @Override
            public LoggerFactory getLoggerFactory() {
                return LoggerFactory.getShared();
            }
        };
    }

    @Override
    public
    final Application getApplication() {
        return lateApplication.get();
    }

    @Override
    public final Logger getStdoutLogger() {
        return getApplication().getStdoutLogger();
    }

    @Override
    public Future<String> deployMe(Application application) {
        lateApplication.set(application);
        return super.deployMe(application.getVertx());
    }
}
