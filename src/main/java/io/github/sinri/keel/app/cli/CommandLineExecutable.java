package io.github.sinri.keel.app.cli;

import io.github.sinri.keel.app.common.AppLifeCycleMixin;
import io.github.sinri.keel.base.json.JsonifiableSerializer;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * CLI 程序的抽象基类。
 * 提供命令行参数处理和程序执行的生命周期管理功能。
 *
 * @since 5.0.0
 */
public abstract class CommandLineExecutable implements AppLifeCycleMixin {

    private CommandLineArguments arguments;

    /**
     * 构建命令行参数解析器的抽象方法。
     * 子类应实现此方法以定义可用的命令行选项和参数。
     *
     * @return 构建的 KeelCliArgsParser 实例，如果不需要解析参数则返回null
     */
    @Nullable
    abstract protected CommandLineArgumentsParser buildCommandLineParser();

    /**
     * 启动CLI程序的入口方法。
     * <p>
     * 应当在启动入口类的 main 方法中调用本方法。
     * <p>
     * 该方法负责初始化参数解析器、解析命令行参数、运行程序逻辑并处理错误。
     *
     * @param args 命令行参数数组
     */
    public final void launch(String[] args) {
        JsonifiableSerializer.register();

        try {
            var argsParser = buildCommandLineParser();
            if (argsParser != null) {
                this.arguments = argsParser.parse(args);
            } else {
                this.arguments = new CommandLineArgumentsImpl();
            }
            runWithCommandLine();
        } catch (Throwable throwable) {
            handleError(throwable);
        }
    }

    /**
     * 获取已解析的命令行参数。
     *
     * @return 已解析的 KeelCliArgs 对象
     * @throws IllegalStateException 如果命令行参数尚未初始化
     */
    @NotNull
    public final CommandLineArguments getArguments() {
        if (arguments == null) {
            throw new IllegalStateException("CliArgs not initialized yet!");
        }
        return arguments;
    }

    /**
     * 处理程序执行过程中的致命错误。
     * 默认实现会将错误信息输出到标准错误流并退出程序。
     *
     * @param throwable 要处理的异常
     */
    public void handleError(Throwable throwable) {
        StdoutLoggerFactory.getInstance().createLogger(getClass().getName())
                           .fatal(log -> log.exception(throwable).message("Program Error"));
        System.exit(1);
    }

    /**
     * 抽象方法，子类应实现具体的业务逻辑。
     * 在此方法中可以访问已解析的命令行参数。
     */
    protected abstract void runWithCommandLine();
}
