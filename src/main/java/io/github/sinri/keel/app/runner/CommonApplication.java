package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.app.cli.CommandLineOption;
import io.github.sinri.keel.app.runner.service.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 基于依次部署给定的 Verticles 运行的应用程序，默认提供了常见的服务封装。
 * <p>
 * 通用应用一般启动的服务依次为：<br>
 * 1. 监控服务<br>
 * 2. 业务初始化服务<br>
 * 3. 队列服务<br>
 * 4. 定时任务服务<br>
 * 5. HTTP 服务<br>
 * <p>
 * 可以通过命令行参数和重载服务构建方法来自定义服务运作。
 *
 * @since 5.0.0
 */
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

}
