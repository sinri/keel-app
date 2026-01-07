package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

@Deprecated(forRemoval = true)
@NullMarked
public abstract class AbstractServiceImpl extends KeelVerticleBase implements Service {
    private final LateObject<Application> lateApplication = new LateObject<>();

    @Override
    public final Application getApplication() {
        return lateApplication.get();
    }

    @Override
    public final Future<String> deployMe(Application application) {
        lateApplication.set(application);
        return deployMe(application.getVertx(), buildDeploymentOptions());
    }

    protected abstract DeploymentOptions buildDeploymentOptions();
}
