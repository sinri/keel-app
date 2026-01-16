package io.github.sinri.keel.app.runner.service;

import io.github.sinri.keel.app.common.AppRecordingMixin;
import io.github.sinri.keel.app.runner.ProgramContext;
import io.github.sinri.keel.base.VertxHolder;
import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.vertx.core.Deployable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

import java.util.function.Function;

/**
 * 在应用体系下运行的服务的通用接口。
 *
 * @since 5.0.0
 */
@NullMarked
public interface Service<P extends ProgramContext> extends AppRecordingMixin, VertxHolder, KeelAsyncMixin, Deployable {
    static <P extends ProgramContext> Service<P> wrap(Function<Service<P>, Future<Void>> anything) {
        return new WrappedService<>(anything);
    }

    /**
     * 部署所在的应用的上下文实例，仅在部署后可获取。
     */
    P getProgramContext();

    Future<String> deployMe(Vertx vertx, P programContext);

    /**
     * 注明本服务是否为不可或缺的，如果是，则当部署失败是触发程序启动异常。
     * <p>
     * 默认为 true。
     *
     * @return 本服务是否为不可或缺的
     */
    default boolean isIndispensableService() {
        return true;
    }
}
