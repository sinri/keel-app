package io.github.sinri.keel.app.common;

import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.metric.MetricRecorder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 程序的记录接口。
 *
 * @since 5.0.0
 */
@NullMarked
public interface AppRecordingMixin {
    /**
     * 获取日志记录器工厂。
     *
     * @return 日志记录器工厂实例
     */
    LoggerFactory getLoggerFactory();

    /**
     * 获取指标记录器。
     * <p>
     * 这是一个可选能力，默认并未提供，需要重载自行加载。
     *
     * @return 指标记录器实例；默认为空。
     */
    default @Nullable MetricRecorder getMetricRecorder() {
        return null;
    }
}
