package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.app.runner.CommonApplication;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 通过 HTTP 协议处理请求的服务。
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class AbstractReceptionistService extends KeelHttpServer implements Service {

    private final LateObject<Application> lateApplication = new LateObject<>();

    public AbstractReceptionistService() {
        super();
    }

    @Override
    public Application getApplication() {
        return lateApplication.get();
    }

    @Override
    public final LoggerFactory getLoggerFactory() {
        return getApplication().getLoggerFactory();
    }

    @Override
    public final Logger getStdoutLogger() {
        return getApplication().getStdoutLogger();
    }

    @Override
    protected int getHttpServerPort() {
        Integer port = readConfiguredListenPort();
        return Objects.requireNonNullElseGet(port, super::getHttpServerPort);
    }

    /**
     * 从运行参数中尝试获取服务监听端口。
     *
     * @return 运行参数中定义的服务监听端口
     * @see AbstractReceptionistService#getHttpServerPort()
     */
    public @Nullable Integer readConfiguredListenPort() {
        String s = getApplication().getArguments().readOption(CommonApplication.optionReceptionistPort);
        return (s == null ? null : Integer.parseInt(s));
    }

    @Override
    public Future<String> deployMe(Application application) {
        lateApplication.set(application);
        return super.deployMe(application.getVertx());
    }
}
