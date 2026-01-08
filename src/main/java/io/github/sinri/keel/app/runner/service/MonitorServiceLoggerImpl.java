package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.common.monitor.MonitorLog;
import io.github.sinri.keel.core.utils.runtime.CPUTimeResult;
import io.github.sinri.keel.core.utils.runtime.GCStatResult;
import io.github.sinri.keel.core.utils.runtime.JVMMemoryResult;
import io.github.sinri.keel.core.utils.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.LogLevel;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * 通过日志体系报告和留存的监控服务实现。
 *
 * @since 5.0.0
 */
@NullMarked
class MonitorServiceLoggerImpl extends AbstractMonitorService {
    private final long startTimestamp;
    private final @Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier;
    private final LateObject<SpecificLogger<MonitorLog>> lateLogger = new LateObject<>();

    public MonitorServiceLoggerImpl(@Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier) {
        super();
        this.startTimestamp = System.currentTimeMillis();
        this.specialSnapshotModifier = specialSnapshotModifier;
    }

    @Override
    protected long getInterval() {
        return 60_000L;
    }

    @Override
    protected Future<Void> startVerticle() {
        lateLogger.set(getLoggerFactory().createLogger(MonitorLog.TopicHealthMonitor, MonitorLog::new));
        return super.startVerticle();
    }

    @Override
    protected void handleMonitorSnapshot(MonitorSnapshot monitorSnapshot) {
        MonitorLog log = new MonitorLog();

        final JsonObject snapshot = new JsonObject();
        snapshot.put("survived", System.currentTimeMillis() - startTimestamp);
        GCStatResult gcStat = monitorSnapshot.gcStat();
        snapshot.put("gc", gcStat.toJsonObject());
        CPUTimeResult cpuTime = monitorSnapshot.cpuTime();
        snapshot.put("cpu_time", cpuTime.toJsonObject());
        JVMMemoryResult jvmMemoryResult = monitorSnapshot.jvmMemoryResult();
        snapshot.put("jvm_memory_stat", jvmMemoryResult.toJsonObject());

        if (specialSnapshotModifier != null) {
            specialSnapshotModifier.accept(monitorSnapshot, snapshot);
        }

        log.snapshot(snapshot);

        double heapUsage = 1.0 * jvmMemoryResult.runtimeHeapUsedBytes() / jvmMemoryResult.runtimeHeapMaxBytes();
        if (cpuTime.cpuUsage() >= 0.50 || heapUsage >= 0.50) {
            log.level(LogLevel.WARNING);
        }
        if (cpuTime.cpuUsage() >= 0.75 || heapUsage >= 0.75 || gcStat.majorGCCount() > 0) {
            log.level(LogLevel.ERROR);
        }

        this.lateLogger.get().log(log);
    }
}
