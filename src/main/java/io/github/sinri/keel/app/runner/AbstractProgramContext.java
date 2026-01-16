package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.app.cli.CommandLineArguments;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.metric.MetricRecorder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * @since 5.0.0
 */
@NullMarked
public class AbstractProgramContext implements ProgramContext {

    private final LateObject<CommandLineArguments> lateArguments = new LateObject<>();
    private final LateObject<MetricRecorder> lateMetricRecorder = new LateObject<>();

    public AbstractProgramContext() {
    }

    @Override
    public @Nullable MetricRecorder getMetricRecorder() {
        if (lateMetricRecorder.isInitialized())
            return lateMetricRecorder.get();
        else return null;
    }

    public void setMetricRecorder(MetricRecorder metricRecorder) {
        lateMetricRecorder.set(metricRecorder);
    }

    @Override
    public CommandLineArguments getParsedCliArguments() {
        return lateArguments.get();
    }

    @Override
    public void setParsedCliArguments(CommandLineArguments arguments) {
        lateArguments.set(arguments);
    }
}
