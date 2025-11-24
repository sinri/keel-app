package io.github.sinri.keel.app.cli;

import io.github.sinri.keel.base.json.JsonifiableSerializer;
import io.github.sinri.keel.base.json.JsonifiedThrowable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * CLI 程序的抽象基类。
 * 提供命令行参数处理和程序执行的生命周期管理功能。
 * @since 5.0.0
 */
public abstract class KeelCliProgram {

    private KeelCliArgs cliArgs;

    /**
     * 构建命令行参数解析器的抽象方法。
     * 子类应实现此方法以定义可用的命令行选项和参数。
     *
     * @return 构建的 KeelCliArgsParser 实例，如果不需要解析参数则返回null
     */
    @Nullable
    abstract protected KeelCliArgsParser buildCliArgParser();

    /**
     * 启动CLI程序的入口方法。
     * 该方法负责初始化参数解析器、解析命令行参数、运行程序逻辑并处理错误。
     *
     * @param args 命令行参数数组
     */
    public final void launch(String[] args) {
        JsonifiableSerializer.register();

        try {
            var argsParser = buildCliArgParser();
            if (argsParser != null) {
                this.cliArgs = argsParser.parse(args);
            } else {
                this.cliArgs = new KeelCliArgsImpl();
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
    public final KeelCliArgs getCliArgs() {
        if (cliArgs == null) {
            throw new IllegalStateException("CliArgs not initialized yet!");
        }
        return cliArgs;
    }

    /**
     * 处理程序执行过程中的错误。
     * 默认实现会将错误信息输出到标准错误流并退出程序。
     *
     * @param throwable 要处理的异常
     */
    protected void handleError(Throwable throwable) {
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);
        System.err.println(jsonifiedThrowable.toJsonObject().encodePrettily());
        if (throwable instanceof KeelCliArgsDefinitionError) {
            System.exit(1);
        } else if (throwable instanceof KeelCliArgsParseError) {
            System.exit(2);
        } else {
            System.exit(3);
        }
    }

    /**
     * 抽象方法，子类应实现具体的业务逻辑。
     * 在此方法中可以访问已解析的命令行参数。
     */
    protected abstract void runWithCommandLine();
}
