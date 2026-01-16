package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.metric.MetricRecorder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 程序上下文，即业务代码运行时依赖的非顶层业务实体。
 *
 * @since 5.0.0
 */
@NullMarked
public interface ProgramContext {

    default @Nullable MetricRecorder getMetricRecorder() {
        return null;
    }

    default void setMetricRecorder(MetricRecorder metricRecorder) {
        throw new UnsupportedOperationException();
    }

    default ConfigElement getRootConfigElement() {
        return ConfigElement.root();
    }

    default LoggerFactory getLoggerFactory() {
        return LoggerFactory.getShared();
    }
}
