package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.common.AppRecordingMixin;
import io.github.sinri.keel.app.runner.Application;
import io.github.sinri.keel.base.VertxHolder;
import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.vertx.core.Deployable;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.function.Function;

/**
 * 在应用体系下运行的服务的通用接口。
 *
 * @since 5.0.0
 */
@NullMarked
public interface Service extends AppRecordingMixin, VertxHolder, KeelAsyncMixin, Deployable {
    static Service wrap(Function<Service, Future<Void>> anything) {
        return new WrappedService(anything);
    }

    /**
     * 部署所在的 {@link Application} 实例，仅在部署后可获取。
     *
     * @return 部署所在的 {@link Application} 实例
     */
    Application getApplication();

    Future<String> deployMe(Application application);
}
