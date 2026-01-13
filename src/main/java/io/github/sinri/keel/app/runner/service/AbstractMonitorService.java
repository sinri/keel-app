package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.core.utils.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.core.utils.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.github.sinri.keel.logger.api.metric.MetricRecord;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
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
public abstract class AbstractMonitorService extends KeelVerticleBase implements Service {
    private final LateObject<Application> lateApplication = new LateObject<>();

    public AbstractMonitorService() {
        super();
    }

    public static AbstractMonitorService throughLogger(@Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier) {
        return new MonitorServiceLoggerImpl(specialSnapshotModifier);
    }

    public static AbstractMonitorService throughMetricRecorder(@Nullable Function<MonitorSnapshot, List<MetricRecord>> specialSnapshotModifier) {
        return new MonitorServiceMetricImpl(specialSnapshotModifier);
    }

    @Override
    public Application getApplication() {
        return lateApplication.get();
    }

    @Override
    protected Future<Void> startVerticle() {
        new KeelRuntimeMonitor(getVertx()).startRuntimeMonitor(getInterval(), this::handleMonitorSnapshot);
        return Future.succeededFuture();
    }

    abstract protected long getInterval();

    abstract protected void handleMonitorSnapshot(MonitorSnapshot monitorSnapshot);

    @Override
    public final Future<String> deployMe(Application application) {
        lateApplication.set(application);
        return deployMe(application.getVertx(), new DeploymentOptions()
                .setThreadingModel(ThreadingModel.WORKER));
    }

    @Override
    public final Logger getStdoutLogger() {
        return getApplication().getStdoutLogger();
    }
}
