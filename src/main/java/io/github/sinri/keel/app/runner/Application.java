package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.app.cli.CommandLineArgumentsParser;
import io.github.sinri.keel.app.cli.CommandLineOption;
import io.github.sinri.keel.app.runner.service.Service;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.github.sinri.keel.base.KeelInstance.Keel;

public abstract class Application extends Program {

    @Override
    protected @Nullable CommandLineArgumentsParser buildCommandLineParser() {
        CommandLineArgumentsParser commandLineArgumentsParser = CommandLineArgumentsParser.create();

        List<CommandLineOption> commandLineOptions = buildCliOptions();
        if (commandLineOptions != null) {
            commandLineOptions.forEach(commandLineArgumentsParser::addOption);
        }

        return commandLineArgumentsParser;
    }


    @Nullable
    protected List<CommandLineOption> buildCliOptions() {
        return null;
    }


    @NotNull
    protected abstract String buildCliName();


    @NotNull
    protected abstract String buildCliDescription();

    @Override
    public void handleError(Throwable throwable) {
        getLogger().error("Program: " + buildCliName());
        getLogger().error("Description: " + buildCliDescription());
        super.handleError(throwable);
    }

    @Override
    protected final Future<Void> launchAsProgram() {
        List<Service> services = buildServices();
        return Keel.asyncCallIteratively(
                           services,
                           service -> service.deployMe()
                                             .compose(deploymentID -> {
                                                 getLogger().info("Deployed verticle %s with deploymentID %s".formatted(
                                                         service.getClass().getName(), deploymentID
                                                 ));
                                                 return Future.succeededFuture();
                                             })
                   )
                   .compose(v -> {
                       getLogger().info("All services deployed");
                       return Future.succeededFuture();
                   });
    }

    @NotNull
    abstract protected List<Service> buildServices();

}
