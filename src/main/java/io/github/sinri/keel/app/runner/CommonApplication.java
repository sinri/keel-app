package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.app.cli.CommandLineOption;
import io.github.sinri.keel.app.runner.service.*;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
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
public abstract class CommonApplication<C extends ProgramContext> extends Application<C> {
    public static final String optionDisableMonitor = "disableMonitor";
    public static final String optionDisableQueue = "disableQueue";
    public static final String optionDisableSundial = "disableSundial";
    public static final String optionDisableReceptionist = "disableReceptionist";
    public static final String optionReceptionistPort = "receptionistPort";

    private final LateObject<AbstractMonitorService<C>> lateMonitorService = new LateObject<>();
    private final LateObject<AbstractQueueService<C>> lateQueueService = new LateObject<>();
    private final LateObject<AbstractSundialService<C>> lateSundialService = new LateObject<>();
    private final LateObject<AbstractReceptionistService<C>> lateReceptionistService = new LateObject<>();

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
        AliyunSlsConfigElement aliyunSlsConfigElement;
        try {
            aliyunSlsConfigElement = AliyunSlsConfigElement.forSls(ConfigElement.root());
        } catch (NotConfiguredException e) {
            getStdoutLogger().warning("Aliyun Sls Config Not Found.");
            aliyunSlsConfigElement = null;
        }
        SlsLoggerFactory slsLoggerFactory = new SlsLoggerFactory(aliyunSlsConfigElement);
        return slsLoggerFactory.deployMe(getVertx(), new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                               .compose(v -> {
                                   return Future.succeededFuture(slsLoggerFactory);
                               }, throwable -> {
                                   System.err.println("Failed to deploy SlsLoggerFactory: " + throwable.getMessage());
                                   return Future.failedFuture(throwable);
                               });
    }

    @Override
    protected List<Service<C>> buildServices() {
        List<Service<C>> services = new ArrayList<>();

        if (!isMonitorDisabled()) {
            AbstractMonitorService<C> monitorService = constructMonitorService();
            if (monitorService != null) {
                lateMonitorService.set(monitorService);
                services.add(monitorService);
            }
        }

        if (!isQueueDisabled()) {
            AbstractQueueService<C> queueService = constructQueueService();
            if (queueService != null) {
                lateQueueService.set(queueService);
                services.add(queueService);
            }
        }

        if (!isSundialDisabled()) {
            AbstractSundialService<C> sundialService = constructSundialService();
            if (sundialService != null) {
                lateSundialService.set(sundialService);
                services.add(sundialService);
            }
        }

        if (!isReceptionistDisabled()) {
            AbstractReceptionistService<C> receptionistService = constructReceptionistService();
            if (receptionistService != null) {
                lateReceptionistService.set(receptionistService);
                services.add(receptionistService);
            }
        }

        return services;
    }

    abstract protected @Nullable AbstractMonitorService<C> constructMonitorService();

    abstract protected @Nullable AbstractQueueService<C> constructQueueService();

    abstract protected @Nullable AbstractSundialService<C> constructSundialService();

    abstract protected @Nullable AbstractReceptionistService<C> constructReceptionistService();

    public AbstractMonitorService<C> getMonitorService() {
        return lateMonitorService.get();
    }

    public AbstractQueueService<C> getQueueService() {
        return lateQueueService.get();
    }

    public AbstractSundialService<C> getSundialService() {
        return lateSundialService.get();
    }

    public AbstractReceptionistService<C> getReceptionistService() {
        return lateReceptionistService.get();
    }

}
