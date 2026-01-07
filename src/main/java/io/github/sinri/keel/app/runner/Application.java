package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.app.cli.CommandLineArgumentsParser;
import io.github.sinri.keel.app.cli.CommandLineOption;
import io.github.sinri.keel.app.runner.service.Service;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;


/**
 * 基于依次部署给定的 Verticles 运行的应用程序。
 *
 * @since 5.0.0
 */
@NullMarked
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

    protected @Nullable List<CommandLineOption> buildCliOptions() {
        return null;
    }

    protected abstract String buildCliName();

    protected abstract String buildCliDescription();

    @Override
    public void handleError(Throwable throwable) {
        getLogger().error("Program: " + buildCliName());
        getLogger().error("Description: " + buildCliDescription());
        super.handleError(throwable);
    }

    @Override
    protected final Future<Void> launchAsProgram() {
        return prepare()
                .compose(prepared -> {
                    List<Service> services = buildServices();
                    return asyncCallIteratively(
                            services,
                            service -> {
                                getLogger().info("For service %s".formatted(service.getClass().getName()));
                                return service.deployMe(this)
                                              .compose(deploymentID -> {
                                                  getLogger().info("Deployed verticle %s with deploymentID %s".formatted(
                                                          service.getClass().getName(), deploymentID
                                                  ));
                                                  return Future.succeededFuture();
                                              }, throwable -> {
                                                  getLogger().error(x -> x.exception(throwable)
                                                                          .message("Failed to deploy verticle %s".formatted(service.getClass()
                                                                                                                                   .getName())));
                                                  return Future.succeededFuture();
                                              });
                            }
                    )
                            .compose(v -> {
                                getLogger().info("All services deployed");
                                return Future.succeededFuture();
                            });
                });
    }


    abstract protected List<Service> buildServices();


    abstract protected Future<Void> prepare();
}
