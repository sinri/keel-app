package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.app.cli.CommandLineOption;
import io.github.sinri.keel.app.runner.service.*;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class CommonApplication extends Application {
    public static final String optionDisableMonitor = "disableMonitor";
    public static final String optionDisableQueue = "disableQueue";
    public static final String optionDisableSundial = "disableSundial";
    public static final String optionDisableReceptionist = "disableReceptionist";
    public static final String optionReceptionistPort = "receptionistPort";

    private MonitorService monitorService;
    private QueueService queueService;
    private SundialService sundialService;
    private ReceptionistService receptionistService;

    @Nullable
    @Override
    protected List<CommandLineOption> buildCliOptions() {
        return List.of(
                new CommandLineOption()
                        .alias(optionDisableQueue)
                        .flag()
                        .description("Disable queue functionality"),
                new CommandLineOption()
                        .alias(optionDisableSundial)
                        .flag()
                        .description("Disable sundial functionality"),
                new CommandLineOption()
                        .alias(optionDisableReceptionist)
                        .flag()
                        .description("Disable receptionist functionality"),
                new CommandLineOption()
                        .alias(optionReceptionistPort)
                        .setValueValidator(s -> {
                            return Pattern.compile("^[1-9][0-9]+$")
                                          .matcher(s)
                                          .matches();
                        })
                        .description("Port for the receptionist"),
                new CommandLineOption()
                        .alias(optionDisableMonitor)
                        .flag()
                        .description("Disable monitor functionality")
        );
    }


    protected boolean isMonitorDisabled() {
        return getArguments().readFlag(optionDisableMonitor);
    }


    protected boolean isQueueDisabled() {
        return getArguments().readFlag(optionDisableQueue);
    }


    protected boolean isSundialDisabled() {
        return getArguments().readFlag(optionDisableSundial);
    }


    protected boolean isReceptionistDisabled() {
        return getArguments().readFlag(optionDisableReceptionist);
    }

    @Override
    protected @NotNull List<Service> buildServices() {
        List<Service> services = new ArrayList<>();

        if (!isMonitorDisabled()) {
            monitorService = constructMonitorService();
            if (monitorService != null) {
                services.add(monitorService);
            }
        }

        services.add(Service.wrap(this, this::prepare));

        if (!isQueueDisabled()) {
            queueService = constructQueueService();
            if (queueService != null) {
                services.add(queueService);
            }
        }

        if (!isSundialDisabled()) {
            sundialService = constructSundialService();
            if (sundialService != null) {
                services.add(sundialService);
            }
        }

        if (!isReceptionistDisabled()) {
            receptionistService = constructReceptionistService();
            if (receptionistService != null) {
                services.add(receptionistService);
            }
        }

        return services;
    }

    abstract protected @Nullable MonitorService constructMonitorService();

    abstract protected @NotNull Future<Void> prepare();

    abstract protected @Nullable QueueService constructQueueService();

    abstract protected @Nullable SundialService constructSundialService();

    abstract protected @Nullable ReceptionistService constructReceptionistService();

    public MonitorService getMonitorService() {
        return monitorService;
    }

    public QueueService getQueueService() {
        return queueService;
    }

    public SundialService getSundialService() {
        return sundialService;
    }

    public ReceptionistService getReceptionistService() {
        return receptionistService;
    }

    @Override
    protected @NotNull VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    @Override
    protected @NotNull LoggerFactory buildLoggerFactory() {
        return StdoutLoggerFactory.getInstance();
    }
}
