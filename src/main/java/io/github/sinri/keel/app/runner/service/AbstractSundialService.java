package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.ProgramContext;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.core.servant.sundial.Sundial;
import io.github.sinri.keel.core.servant.sundial.SundialPlan;
import io.github.sinri.keel.logger.api.LateObject;
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
public abstract class AbstractSundialService<P extends ProgramContext> extends Sundial implements Service<P> {

    private final LateObject<P> lateProgramContext = new LateObject<>();

    public AbstractSundialService() {
        super();
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
            public Future<String> deployMe(Keel keel, P programContext) {
                return deployMe(keel);
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

    @Override
    public Future<String> deployMe(Keel keel, P programContext) {
        lateProgramContext.set(programContext);
        return super.deployMe(keel);
    }
}
