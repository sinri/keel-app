package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.app.cli.CommandLineExecutable;
import io.github.sinri.keel.app.common.AppRecordingMixin;
import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.json.JsonifiableSerializer;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.LateObject;
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
public abstract class Program extends CommandLineExecutable implements AppRecordingMixin, Keel {

    // private final ConfigElement rootConfigElement;
    private final LateObject<Vertx> lateVertx = new LateObject<>();
    // private LoggerFactory loggerFactory;
    private Logger logger;
    private @Nullable MetricRecorder metricRecorder;

    public Program() {
        // this.rootConfigElement = new ConfigElement("");
        // this.loggerFactory = StdoutLoggerFactory.getInstance();
        this.resetLogger();
    }

    //    @Override
    //    public ConfigElement getConfiguration() {
    //        return rootConfigElement;
    //    }

    @Override
    public @Nullable MetricRecorder getMetricRecorder() {
        return metricRecorder;
    }

    @Override
    public void handleError(Throwable throwable) {
        this.getLogger().fatal(log -> {
            log.exception(throwable);
            log.message("Program Error");
        });
        System.exit(1);
    }

    @Override
    protected final void runWithCommandLine() {
        long startTime = System.currentTimeMillis();

        JsonifiableSerializer.register();

        try {
            loadLocalConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.getLogger().info("LOCAL CONFIG LOADED (if any)");

        VertxOptions vertxOptions = buildVertxOptions();
        ClusterManager clusterManager = buildClusterManager();

        Future.succeededFuture()
              .compose(v -> {
                  if (clusterManager == null) {
                      // NOT SUPPORT CLUSTER MODE
                      Vertx tempVertx = Vertx.builder().with(vertxOptions).build();
                      lateVertx.set(tempVertx);
                      return Future.succeededFuture();
                  } else {
                      return Vertx.builder().withClusterManager(clusterManager).with(vertxOptions).buildClustered()
                                  .compose(built -> {
                                      this.lateVertx.set(built);
                                      return Future.succeededFuture();
                                  });
                  }
              })
              .compose(initialized -> {
                  this.getLogger().info("KEEL INITIALIZED");
                  return loadRemoteConfiguration();
              })
              .compose(done -> {
                  this.getLogger().info("REMOTE CONFIG LOADED (if any)");

                  // customized logging
                  return buildLoggerFactory()
                          .compose(builtLoggerFactory -> {
                              if (builtLoggerFactory != SHARED_LOGGER_FACTORY_REF.get()) {
                                  //loggerFactory = builtLoggerFactory;
                                  SHARED_LOGGER_FACTORY_REF.set(builtLoggerFactory);
                                  this.resetLogger();
                                  getLogger().info("CUSTOM LOGGER FACTORY CENTER LOADED");
                              }
                              return Future.succeededFuture();
                          });
              })
              .compose(v -> {
                  // metric recording
                  return buildMetricRecorder()
                          .compose(builtMetricRecorder -> {
                              if (builtMetricRecorder != null) {
                                  this.metricRecorder = builtMetricRecorder;
                                  getLogger().info("CUSTOM METRIC RECORDER LOADED");
                              }
                              return Future.succeededFuture();
                          });
              })
              .compose(v -> {
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
        getConfiguration().loadPropertiesFile("config.properties");
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

    @Override
    public final LoggerFactory getLoggerFactory() {
        // return loggerFactory;
        return Keel.super.getLoggerFactory();
    }

    protected Future<LoggerFactory> buildLoggerFactory() {
        return Future.succeededFuture(StdoutLoggerFactory.getInstance());
    }

    private void resetLogger() {
        this.logger = this.getLoggerFactory().createLogger(getClass().getName());
    }


    public final Logger getLogger() {
        return logger;
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
        this.getLogger().notice("Warship launched, spent " + (endTime - startTime) + " ms");
    }

    protected void affix() {
        // do nothing by default, or you may need a latch to keep the main process alive.
    }

    public final Vertx getVertx() {
        return lateVertx.get();
    }
}
