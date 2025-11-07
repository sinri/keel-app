package io.github.sinri.keel.app.cli;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.core.json.JsonifiableSerializer;
import io.github.sinri.keel.core.json.JsonifiedThrowable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 4.1.3
 */
@TechnicalPreview(since = "4.1.3")
public abstract class KeelCliProgram {

    private KeelCliArgs cliArgs;

    @Nullable
    abstract protected KeelCliArgsParser buildCliArgParser();

    public final void launch(String[] args) {
        JsonifiableSerializer.register();

        try {
            var argsParser = buildCliArgParser();
            if (argsParser != null) {
                this.cliArgs = argsParser.parse(args);
            } else {
                this.cliArgs = new KeelCliArgsImpl();
            }
            runWithCommandLine();
        } catch (Throwable throwable) {
            handleError(throwable);
        }
    }

    @Nonnull
    public final KeelCliArgs getCliArgs() {
        if (cliArgs == null) {
            throw new IllegalStateException("CliArgs not initialized yet!");
        }
        return cliArgs;
    }

    protected void handleError(Throwable throwable) {
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);
        System.err.println(jsonifiedThrowable.toJsonObject().encodePrettily());
        if (throwable instanceof KeelCliArgsDefinitionError) {
            System.exit(1);
        } else if (throwable instanceof KeelCliArgsParseError) {
            System.exit(2);
        } else {
            System.exit(3);
        }
    }

    protected abstract void runWithCommandLine();
}
