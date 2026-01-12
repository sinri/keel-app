package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.app.cli.CommandLineOption;
import io.github.sinri.keel.app.runner.service.*;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.integration.aliyun.sls.AliyunSlsConfigElement;
import io.github.sinri.keel.integration.aliyun.sls.SlsLoggerFactory;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
@NullMarked
public abstract class CommonApplication extends Application {
    public static final String optionDisableMonitor = "disableMonitor";
    public static final String optionDisableQueue = "disableQueue";
    public static final String optionDisableSundial = "disableSundial";
    public static final String optionDisableReceptionist = "disableReceptionist";
    public static final String optionReceptionistPort = "receptionistPort";

    private final LateObject<AbstractMonitorService> lateMonitorService = new LateObject<>();
    private final LateObject<AbstractQueueService> lateQueueService = new LateObject<>();
    private final LateObject<AbstractSundialService> lateSundialService = new LateObject<>();
    private final LateObject<AbstractReceptionistService> lateReceptionistService = new LateObject<>();

    @Override
    protected @Nullable List<CommandLineOption> buildCliOptions() {
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
    protected Future<LoggerFactory> buildLoggerFactory() {
        AliyunSlsConfigElement aliyunSlsConfigElement = AliyunSlsConfigElement.forSls(ConfigElement.root());
        SlsLoggerFactory slsLoggerFactory = new SlsLoggerFactory(aliyunSlsConfigElement);
        return slsLoggerFactory.deployMe(getVertx(), new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                               .compose(v -> {
                                   return Future.succeededFuture(slsLoggerFactory);
                               });
    }

    @Override
    protected List<Service> buildServices() {
        List<Service> services = new ArrayList<>();

        if (!isMonitorDisabled()) {
            AbstractMonitorService monitorService = constructMonitorService();
            if (monitorService != null) {
                lateMonitorService.set(monitorService);
                services.add(monitorService);
            }
        }

        if (!isQueueDisabled()) {
            AbstractQueueService queueService = constructQueueService();
            if (queueService != null) {
                lateQueueService.set(queueService);
                services.add(queueService);
            }
        }

        if (!isSundialDisabled()) {
            AbstractSundialService sundialService = constructSundialService();
            if (sundialService != null) {
                lateSundialService.set(sundialService);
                services.add(sundialService);
            }
        }

        if (!isReceptionistDisabled()) {
            AbstractReceptionistService receptionistService = constructReceptionistService();
            if (receptionistService != null) {
                lateReceptionistService.set(receptionistService);
                services.add(receptionistService);
            }
        }

        return services;
    }

    abstract protected @Nullable AbstractMonitorService constructMonitorService();

    abstract protected @Nullable AbstractQueueService constructQueueService();

    abstract protected @Nullable AbstractSundialService constructSundialService();

    abstract protected @Nullable AbstractReceptionistService constructReceptionistService();

    public AbstractMonitorService getMonitorService() {
        return lateMonitorService.get();
    }

    public AbstractQueueService getQueueService() {
        return lateQueueService.get();
    }

    public AbstractSundialService getSundialService() {
        return lateSundialService.get();
    }

    public AbstractReceptionistService getReceptionistService() {
        return lateReceptionistService.get();
    }

}
