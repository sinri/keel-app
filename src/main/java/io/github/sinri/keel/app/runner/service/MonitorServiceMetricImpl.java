package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.core.utils.runtime.CPUTimeResult;
import io.github.sinri.keel.core.utils.runtime.GCStatResult;
import io.github.sinri.keel.core.utils.runtime.JVMMemoryResult;
import io.github.sinri.keel.core.utils.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.metric.MetricRecord;
import io.github.sinri.keel.logger.api.metric.MetricRecorder;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 通过指标记录体系报告和留存的监控服务实现。
 *
 * @since 5.0.0
 */
@NullMarked
class MonitorServiceMetricImpl extends AbstractMonitorService {
    public static final String METRIC_SURVIVED = "survived";
    public static final String METRIC_MINOR_GC_COUNT = "minor_gc_count";
    public static final String METRIC_MINOR_GC_TIME = "minor_gc_time";
    public static final String METRIC_MAJOR_GC_COUNT = "major_gc_count";
    public static final String METRIC_MAJOR_GC_TIME = "major_gc_time";
    public static final String METRIC_CPU_USAGE = "cpu_usage";
    public static final String METRIC_HARDWARE_MEMORY_USAGE = "hardware_memory_usage";
    public static final String METRIC_JVM_MEMORY_USAGE = "jvm_memory_usage";
    public static final String METRIC_JVM_HEAP_MEMORY_USED_BYTES = "jvm_heap_memory_used_bytes";
    public static final String METRIC_JVM_NON_HEAP_MEMORY_USED_BYTES = "jvm_non_heap_memory_used_bytes";
    private final long startTimestamp;
    private final @Nullable Function<MonitorSnapshot, List<MetricRecord>> specialSnapshotModifier;
    private final LateObject<MetricRecorder> lateMetricRecorder = new LateObject<>();

    public MonitorServiceMetricImpl(@Nullable Function<MonitorSnapshot, List<MetricRecord>> specialSnapshotModifier) {
        super();
        this.startTimestamp = System.currentTimeMillis();
        this.specialSnapshotModifier = specialSnapshotModifier;
        // this.metricRecorder = Objects.requireNonNull(application.getMetricRecorder());
    }

    @Override
    protected long getInterval() {
        return 10_000L;
    }

    @Override
    protected Future<Void> startVerticle() {
        lateMetricRecorder.set(Objects.requireNonNull(getApplication().getMetricRecorder()));
        return super.startVerticle();
    }

    @Override
    protected void handleMonitorSnapshot(MonitorSnapshot monitorSnapshot) {
        long now = System.currentTimeMillis();

        MetricRecorder metricRecorder = lateMetricRecorder.get();

        metricRecorder.recordMetric(MetricRecord.create(
                now,
                METRIC_SURVIVED,
                System.currentTimeMillis() - startTimestamp,
                null
        ));

        JVMMemoryResult jvmMemoryResult = monitorSnapshot.jvmMemoryResult();
        metricRecorder.recordMetric(MetricRecord.create(
                jvmMemoryResult.statTime(),
                METRIC_HARDWARE_MEMORY_USAGE,
                1.0 * jvmMemoryResult.physicalUsedBytes()
                        / jvmMemoryResult.physicalMaxBytes(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                monitorSnapshot.jvmMemoryResult().statTime(),
                METRIC_JVM_MEMORY_USAGE,
                1.0 * jvmMemoryResult.runtimeHeapUsedBytes()
                        / jvmMemoryResult.runtimeHeapMaxBytes(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                jvmMemoryResult.statTime(),
                METRIC_JVM_HEAP_MEMORY_USED_BYTES,
                jvmMemoryResult.mxHeapUsedBytes(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                jvmMemoryResult.statTime(),
                METRIC_JVM_NON_HEAP_MEMORY_USED_BYTES,
                jvmMemoryResult.mxNonHeapUsedBytes(),
                null
        ));

        CPUTimeResult cpuTime = monitorSnapshot.cpuTime();
        metricRecorder.recordMetric(MetricRecord.create(
                cpuTime.statTime(),
                METRIC_CPU_USAGE,
                cpuTime.cpuUsage(),
                null
        ));

        GCStatResult gcStat = monitorSnapshot.gcStat();
        metricRecorder.recordMetric(MetricRecord.create(
                gcStat.statTime(),
                METRIC_MAJOR_GC_COUNT,
                gcStat.majorGCCount(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                gcStat.statTime(),
                METRIC_MAJOR_GC_TIME,
                gcStat.majorGCTime(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                gcStat.statTime(),
                METRIC_MINOR_GC_COUNT,
                gcStat.minorGCCount(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                gcStat.statTime(),
                METRIC_MINOR_GC_TIME,
                gcStat.minorGCTime(),
                null
        ));

        if (this.specialSnapshotModifier != null) {
            List<MetricRecord> list = this.specialSnapshotModifier.apply(monitorSnapshot);
            if (!list.isEmpty()) {
                list.forEach(metricRecorder::recordMetric);
            }
        }
    }
}
