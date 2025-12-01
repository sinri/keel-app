package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.common.monitor.MonitorLog;
import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.core.utils.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.core.utils.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.api.LogLevel;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.metric.MetricRecord;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public abstract class MonitorService extends AbstractKeelVerticle implements Service {
    @NotNull
    private final Application application;

    public MonitorService(@NotNull Application application) {
        super(application);
        this.application = application;
    }

    public static MonitorService throughLogger(@NotNull Application application, @Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier) {
        return new MonitorServiceLoggerImpl(application, specialSnapshotModifier);
    }

    public static MonitorService throughMetricRecorder(@NotNull Application application, @Nullable Function<MonitorSnapshot, List<MetricRecord>> specialSnapshotModifier) {
        return new MonitorServiceMetricImpl(application, specialSnapshotModifier);
    }

    protected static MonitorLog generateLogForMonitorSnapshot(
            long startTimestamp,
            @NotNull MonitorSnapshot monitorSnapshot,
            @Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier
    ) {
        MonitorLog log = new MonitorLog();

        final JsonObject snapshot = new JsonObject()
                .put("survived", System.currentTimeMillis() - startTimestamp)
                .put("gc", monitorSnapshot.getGCStat().toJsonObject())
                .put("cpu_time", monitorSnapshot.getCPUTime().toJsonObject())
                .put("jvm_memory_stat", monitorSnapshot.getJvmMemoryResult().toJsonObject());

        if (specialSnapshotModifier != null) {
            specialSnapshotModifier.accept(monitorSnapshot, snapshot);
        }

        log.snapshot(snapshot);

        double heapUsage = 1.0 * monitorSnapshot.getJvmMemoryResult().getRuntimeHeapUsedBytes()
                / monitorSnapshot.getJvmMemoryResult().getRuntimeHeapMaxBytes();
        if (monitorSnapshot.getCPUTime().getCpuUsage() >= 0.50 || heapUsage >= 0.50) {
            log.level(LogLevel.WARNING);
        }
        if (monitorSnapshot.getCPUTime().getCpuUsage() >= 0.75 || heapUsage >= 0.75
                || monitorSnapshot.getGCStat().getMajorGCCount() > 0) {
            log.level(LogLevel.ERROR);
        }
        return log;
    }

    @Override
    public @NotNull Application getApplication() {
        return application;
    }

    @Override
    public Future<String> deployMe() {
        return deployMe(new DeploymentOptions()
                .setThreadingModel(ThreadingModel.WORKER));
    }

    @Override
    public @NotNull LoggerFactory getLoggerFactory() {
        return application.getLoggerFactory();
    }

    @Override
    protected @NotNull Future<Void> startVerticle() {
        new KeelRuntimeMonitor(getVertx()).startRuntimeMonitor(getInterval(), this::handleMonitorSnapshot);
        return Future.succeededFuture();
    }

    abstract protected long getInterval();

    abstract protected void handleMonitorSnapshot(MonitorSnapshot monitorSnapshot);
}
