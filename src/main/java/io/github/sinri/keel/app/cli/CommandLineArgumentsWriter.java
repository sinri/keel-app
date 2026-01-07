package io.github.sinri.keel.app.cli;


import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 用于记录和写入命令行参数解析结果的接口。
 *
 * @since 5.0.0
 */
@NullMarked
interface CommandLineArgumentsWriter {
    /**
     * 创建 KeelCliArgsWriter 实例的工厂方法。
     *
     * @return 新的 KeelCliArgsWriter 实例
     */
    static CommandLineArgumentsWriter create() {
        return new CommandLineArgumentsImpl();
    }

    /**
     * 记录一个位置参数。
     *
     * @param parameter 要记录的位置参数
     */
    void recordParameter(String parameter);

    /**
     * 记录一个选项及其值。
     *
     * @param option 要记录的选项
     * @param value  选项的值，如果选项是标志则为null
     */
    void recordOption(CommandLineOption option, @Nullable String value);

    /**
     * 将当前的记录结果转换为 KeelCliArgs 对象。
     *
     * @return 包含所有记录选项和参数的 KeelCliArgs 对象
     */
    CommandLineArguments toResult();
}
