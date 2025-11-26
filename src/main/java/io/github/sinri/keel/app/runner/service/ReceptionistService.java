package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.app.runner.CommonApplication;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.web.http.KeelHttpServer;
import org.jetbrains.annotations.NotNull;

public abstract class ReceptionistService extends KeelHttpServer implements Service {
    @NotNull
    private final Application application;

    public ReceptionistService(@NotNull Application application) {
        this.application = application;
    }

    @Override
    public @NotNull Application getApplication() {
        return application;
    }

    @Override
    public @NotNull LoggerFactory getLoggerFactory() {
        return this.application.getLoggerFactory();
    }

    public int getListenPort() {
        String s = application.getArguments().readOption(CommonApplication.optionReceptionistPort);
        return (s == null ? 8080 : Integer.parseInt(s));
    }
}
