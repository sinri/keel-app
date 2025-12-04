package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.core.utils.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.api.metric.MetricRecord;
import io.github.sinri.keel.logger.api.metric.MetricRecorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 通过指标记录体系报告和留存的监控服务实现。
 *
 * @since 5.0.0
 */
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
    protected @NotNull MetricRecorder metricRecorder;

    public MonitorServiceMetricImpl(@NotNull Application application, @Nullable Function<MonitorSnapshot, List<MetricRecord>> specialSnapshotModifier) {
        super(application);
        this.startTimestamp = System.currentTimeMillis();
        this.specialSnapshotModifier = specialSnapshotModifier;
        this.metricRecorder = Objects.requireNonNull(application.getMetricRecorder());
    }

    @Override
    protected long getInterval() {
        return 10_000L;
    }

    @Override
    protected void handleMonitorSnapshot(MonitorSnapshot monitorSnapshot) {
        long now = System.currentTimeMillis();

        metricRecorder.recordMetric(MetricRecord.create(
                now,
                METRIC_SURVIVED,
                System.currentTimeMillis() - startTimestamp,
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                monitorSnapshot.getJvmMemoryResult().getStatTime(),
                METRIC_HARDWARE_MEMORY_USAGE,
                1.0 * monitorSnapshot.getJvmMemoryResult().getPhysicalUsedBytes()
                        / monitorSnapshot.getJvmMemoryResult().getPhysicalMaxBytes(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                monitorSnapshot.getJvmMemoryResult().getStatTime(),
                METRIC_JVM_MEMORY_USAGE,
                1.0 * monitorSnapshot.getJvmMemoryResult().getRuntimeHeapUsedBytes()
                        / monitorSnapshot.getJvmMemoryResult().getRuntimeHeapMaxBytes(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                monitorSnapshot.getJvmMemoryResult().getStatTime(),
                METRIC_JVM_HEAP_MEMORY_USED_BYTES,
                monitorSnapshot.getJvmMemoryResult().getMxHeapUsedBytes(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                monitorSnapshot.getJvmMemoryResult().getStatTime(),
                METRIC_JVM_NON_HEAP_MEMORY_USED_BYTES,
                monitorSnapshot.getJvmMemoryResult().getMxNonHeapUsedBytes(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                monitorSnapshot.getCPUTime().getStatTime(),
                METRIC_CPU_USAGE,
                monitorSnapshot.getCPUTime().getCpuUsage(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                monitorSnapshot.getGCStat().getStatTime(),
                METRIC_MAJOR_GC_COUNT,
                monitorSnapshot.getGCStat().getMajorGCCount(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                monitorSnapshot.getGCStat().getStatTime(),
                METRIC_MAJOR_GC_TIME,
                monitorSnapshot.getGCStat().getMajorGCTime(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                monitorSnapshot.getGCStat().getStatTime(),
                METRIC_MINOR_GC_COUNT,
                monitorSnapshot.getGCStat().getMinorGCCount(),
                null
        ));
        metricRecorder.recordMetric(MetricRecord.create(
                monitorSnapshot.getGCStat().getStatTime(),
                METRIC_MINOR_GC_TIME,
                monitorSnapshot.getGCStat().getMinorGCTime(),
                null
        ));
        if (this.specialSnapshotModifier != null) {
            List<MetricRecord> list = this.specialSnapshotModifier.apply(monitorSnapshot);
            if (list != null) {
                list.forEach(metricRecorder::recordMetric);
            }
        }
    }
}
