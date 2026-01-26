package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.ProgramContext;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.core.utils.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.core.utils.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.metric.MetricRecord;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.ThreadingModel;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 运行时监控服务。
 * <p>
 * 运行时监控服务，用于收集和报告应用程序的运行时状态。
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class AbstractMonitorService<P extends ProgramContext> extends KeelRuntimeMonitor implements Service<P> {
    private final LateObject<P> lateProgramContext = new LateObject<>();
    private final LateObject<Handler<MonitorSnapshot>> lateHandler = new LateObject<>();

    public AbstractMonitorService() {
        super();
    }

    public static <P extends ProgramContext> AbstractMonitorService<P> throughLogger(@Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier) {
        return new MonitorServiceLoggerImpl<>(specialSnapshotModifier);
    }

    public static <P extends ProgramContext> AbstractMonitorService<P> throughMetricRecorder(@Nullable Function<MonitorSnapshot, List<MetricRecord>> specialSnapshotModifier) {
        return new MonitorServiceMetricImpl<>(specialSnapshotModifier);
    }

    @Override
    public final P getProgramContext() {
        return lateProgramContext.get();
    }

    @Override
    protected Future<Void> startVerticle() {
        return super.startVerticle()
                    .compose(v -> {
                        return Future.succeededFuture();
                    });
    }

    @Override
    abstract protected long getInterval();

    @Override
    protected final Handler<MonitorSnapshot> getHandler() {
        return lateHandler.ensure(() -> (Handler<MonitorSnapshot>) this::handleMonitorSnapshot);
    }

    abstract protected void handleMonitorSnapshot(MonitorSnapshot monitorSnapshot);

    @Override
    public Future<String> deployMe(Keel keel, P programContext) {
        lateProgramContext.set(programContext);
        return deployMe(keel, new DeploymentOptions()
                .setThreadingModel(ThreadingModel.WORKER));
    }
}
