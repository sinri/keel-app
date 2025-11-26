package io.github.sinri.keel.app.cli;

import io.github.sinri.keel.app.common.AppLifeCycleMixin;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;


/**
 * CLI 程序的抽象基类。
 * 提供命令行参数处理和程序执行的生命周期管理功能。
 *
 * @since 5.0.0
 */
public abstract class CommandLineExecutable implements AppLifeCycleMixin {

    private final AtomicReference<CommandLineArguments> argumentsRef = new AtomicReference<>();

    /**
     * 构建命令行参数解析器的抽象方法。
     * 子类应实现此方法以定义可用的命令行选项和参数。
     *
     * @return 构建的 {@link CommandLineArgumentsParser} 实例，如果不需要解析参数则返回null
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
    @Override
    public final void launch(String[] args) {
        try {
            var argsParser = buildCommandLineParser();
            if (argsParser != null) {
                this.argumentsRef.set(argsParser.parse(args));
            } else {
                this.argumentsRef.set(new CommandLineArgumentsImpl());
            }
            runWithCommandLine();
        } catch (Throwable throwable) {
            handleError(throwable);
        }
    }

    /**
     * 获取已解析的命令行参数。
     *
     * @return 已解析的 {@link CommandLineArguments} 对象
     * @throws IllegalStateException 如果命令行参数尚未初始化
     */
    @NotNull
    public final CommandLineArguments getArguments() {
        var arguments = argumentsRef.get();
        if (arguments == null) {
            throw new IllegalStateException("CliArgs not initialized yet!");
        }
        return arguments;
    }

    /**
     * 处理程序执行过程中的致命错误。
     * <p>
     * 默认实现会将错误信息输出到标准错误流并以返回值 1 退出程序。
     * 可以按需重写此方法以自定义错误处理逻辑。
     *
     * @param throwable 要处理的异常
     */
    @Override
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
