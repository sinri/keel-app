package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.app.runner.CommonApplication;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

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
    public LoggerFactory getLoggerFactory() {
        return this.getApplication().getLoggerFactory();
    }

    public int getListenPort() {
        String s = getApplication().getArguments().readOption(CommonApplication.optionReceptionistPort);
        return (s == null ? 8080 : Integer.parseInt(s));
    }

    @Override
    public Future<String> deployMe(Application application) {
        lateApplication.set(application);
        return super.deployMe(application.getVertx());
    }
}
