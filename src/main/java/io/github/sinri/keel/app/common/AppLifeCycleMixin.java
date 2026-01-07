package io.github.sinri.keel.app.common;

import org.jspecify.annotations.NullMarked;

/**
 * 程序的生命周期定义接口。
 *
 * @since 5.0.0
 */
@NullMarked
public interface AppLifeCycleMixin {
    /**
     * 启动程序的入口方法。
     * <p>
     * 应当在启动入口类的 main 方法中调用本方法。
     * <p>
     * 该方法根据命令行参数运行程序逻辑。
     *
     * @param args 命令行参数数组
     */
    void launch(String[] args);

    /**
     * 处理程序执行过程中的致命错误。
     *
     * @param throwable 要处理的异常
     */
    void handleError(Throwable throwable);

}
