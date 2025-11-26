package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.common.monitor.MonitorLog;
import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.core.utils.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.api.LogLevel;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

class MonitorServiceLoggerImpl extends MonitorService {
    private final long startTimestamp;
    private final @Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier;
    private final @NotNull SpecificLogger<MonitorLog> logger;

    public MonitorServiceLoggerImpl(@NotNull Application application, @Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier) {
        super(application);
        this.startTimestamp = System.currentTimeMillis();
        this.specialSnapshotModifier = specialSnapshotModifier;
        this.logger = getLoggerFactory().createLogger(MonitorLog.TopicHealthMonitor, MonitorLog::new);
    }

    @Override
    protected long getInterval() {
        return 60_000L;
    }

    @Override
    protected void handleMonitorSnapshot(MonitorSnapshot monitorSnapshot) {
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

        this.logger.log(log);
    }
}
