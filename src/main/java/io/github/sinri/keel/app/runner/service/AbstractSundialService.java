package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.ProgramContext;
import io.github.sinri.keel.core.servant.sundial.Sundial;
import io.github.sinri.keel.core.servant.sundial.SundialPlan;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
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
public abstract class AbstractSundialService<P extends ProgramContext> extends Sundial implements Service<P> {

    private final LateObject<P> lateProgramContext = new LateObject<>();
    //    private final Logger logger;

    public AbstractSundialService() {
        super();
        //        this.logger = StdoutLoggerFactory.getInstance().createLogger(getClass().getName());
    }

    public static <P extends ProgramContext> AbstractSundialService<P> wrap(
            P programContext,
            Supplier<Future<@Nullable Collection<SundialPlan>>> plansFetcher
    ) {
        return new AbstractSundialService<P>() {
            @Override
            public P getProgramContext() {
                return programContext;
            }

            @Override
            public Future<String> deployMe(Vertx vertx, P programContext) {
                return deployMe(vertx);
            }

            @Override
            protected Future<@Nullable Collection<SundialPlan>> fetchPlans() {
                return plansFetcher.get();
            }

        };
    }

    @Override
    public P getProgramContext() {
        return lateProgramContext.get();
    }

    //    @Override
    //    public final Logger getStdoutLogger() {
    //        return logger;
    //    }

    @Override
    public Future<String> deployMe(Vertx vertx, P programContext) {
        lateProgramContext.set(programContext);
        return super.deployMe(vertx);
    }
}
