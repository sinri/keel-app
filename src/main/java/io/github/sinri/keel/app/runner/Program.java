package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.app.cli.CommandLineExecutable;
import io.github.sinri.keel.app.common.AppRecordingMixin;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.json.JsonifiableSerializer;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.base.logger.factory.VertxLoggerDelegateFactoryWorker;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.github.sinri.keel.logger.api.metric.MetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;


/**
 * 本类抽象了基于 Vertx 异步框架运行的程序。
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class Program<C extends ProgramContext> extends CommandLineExecutable implements AppRecordingMixin {
    /**
     * 面向标准输出的日志记录器。
     */
    private final Logger loggerToStdout;

    private final C programContext;

    public Program() {
        super();
        this.loggerToStdout = StdoutLoggerFactory.getInstance().createLogger(this.getClass().getName());
        this.programContext = buildProgramContext();
    }

    abstract protected C buildProgramContext();

    public final C getProgramContext() {
        return programContext;
    }

    @Override
    public @Nullable MetricRecorder getMetricRecorder() {
        return programContext.getMetricRecorder();
    }

    @Override
    public void handleError(Throwable throwable) {
        this.getStdoutLogger().fatal(log -> {
            log.exception(throwable);
            log.message("Program Error");
        });
        System.exit(1);
    }

    @Override
    protected final void runWithCommandLine() {
        long startTime = System.currentTimeMillis();

        VertxLoggerDelegateFactoryWorker.ensureProperty();

        this.getProgramContext().setParsedCliArguments(getArguments());

        JsonifiableSerializer.register();

        try {
            loadLocalConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.getStdoutLogger().info("LOCAL CONFIG LOADED (if any)");

        VertxOptions vertxOptions = buildVertxOptions();
        ClusterManager clusterManager = buildClusterManager();

        Future.succeededFuture()
              .compose(v -> {
                  if (clusterManager == null) {
                      // NOT SUPPORT CLUSTER MODE
                      Vertx tempVertx = Vertx.builder().with(vertxOptions).build();
                      Keel.share(tempVertx);
                      return Future.succeededFuture();
                  } else {
                      return Vertx.builder().withClusterManager(clusterManager).with(vertxOptions).buildClustered()
                                  .compose(clusteredVertx -> {
                                      Keel.share(clusteredVertx);
                                      return Future.succeededFuture();
                                  });
                  }
              })
              .compose(initialized -> {
                  this.getStdoutLogger().info("KEEL INITIALIZED");
                  return loadRemoteConfiguration();
              })
              .compose(done -> {
                  this.getStdoutLogger().info("REMOTE CONFIG LOADED (if any)");

                  // customized logging
                  LoggerFactory existedLoggerFactory = LoggerFactory.getShared();
                  return buildLoggerFactory()
                          .compose(builtLoggerFactory -> {
                              this.getStdoutLogger()
                                  .info("BUILT LOGGER FACTORY CENTER: " + builtLoggerFactory.getClass().getName());
                              if (builtLoggerFactory != existedLoggerFactory) {
                                  LoggerFactory.replaceShared(builtLoggerFactory);
                                  getStdoutLogger().info("CUSTOM LOGGER FACTORY CENTER LOADED");
                              }
                              return Future.succeededFuture();
                          });
              })
              .compose(v -> {
                  // metric recording
                  return buildMetricRecorder()
                          .compose(builtMetricRecorder -> {
                              if (builtMetricRecorder != null) {
                                  getStdoutLogger().info("BUILT METRIC RECORDER: " + builtMetricRecorder.getClass()
                                                                                                        .getName());
                                  this.programContext.setMetricRecorder(builtMetricRecorder);
                                  getStdoutLogger().info("CUSTOM METRIC RECORDER LOADED");
                              }
                              return Future.succeededFuture();
                          });
              })
              .compose(v -> {
                  getStdoutLogger().info("LAUNCHING AS PROGRAM");
                  return launchAsProgram();
              })
              .onSuccess(done -> {
                  whenLaunched(startTime);
              })
              .onFailure(this::handleError);

        // do something after launching async, back in the sync thread
        affix();
    }

    protected void loadLocalConfiguration() throws IOException {
        ConfigElement.root().loadPropertiesFile("config.properties");
    }

    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
    }


    protected VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    @Nullable
    protected ClusterManager buildClusterManager() {
        return null;
    }

    protected Future<LoggerFactory> buildLoggerFactory() {
        return Future.succeededFuture(StdoutLoggerFactory.getInstance());
    }

    /**
     * 面向标准输出的日志记录器，用于程序底层日志。
     *
     * @return 面向标准输出的日志记录器。
     */
    public final Logger getStdoutLogger() {
        return loggerToStdout;
    }


    abstract protected Future<Void> launchAsProgram();

    /**
     * 如果程序需要定量指标记录器，则应构建一个定量指标记录器实例，并确保其可运作。
     * <p>
     * 默认返回 null，即认为程序不需要定量指标记录器。
     *
     * @return 一个可用的定量指标记录器，或 null。
     */

    protected Future<@Nullable MetricRecorder> buildMetricRecorder() {
        return Future.succeededFuture(null);
    }

    protected void whenLaunched(long startTime) {
        long endTime = System.currentTimeMillis();
        this.getStdoutLogger().notice("Warship launched, spent " + (endTime - startTime) + " ms");
    }

    protected void affix() {
        // do nothing by default, or you may need a latch to keep the main process alive.
    }

    public final Keel getKeel() {
        return Keel.shared();
    }
}
