package io.github.sinri.keel.app.common;

import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.metric.MetricRecorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 程序的记录接口。
 *
 * @since 5.0.0
 */
public interface AppRecordingMixin {
    /**
     * 获取日志记录器工厂。
     * <p>
     * 这是必备能力，默认提供{@link StdoutLoggerFactory}单例。
     *
     * @return 日志记录器工厂实例
     */
    @NotNull
    default LoggerFactory getLoggerFactory() {
        return StdoutLoggerFactory.getInstance();
    }

    /**
     * 获取指标记录器。
     * <p>
     * 这是一个可选能力，默认并未提供，需要重载自行加载。
     *
     * @return 指标记录器实例；默认为空。
     */
    @Nullable
    default MetricRecorder getMetricRecorder() {
        return null;
    }
}
