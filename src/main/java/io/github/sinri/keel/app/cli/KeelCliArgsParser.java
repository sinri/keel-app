package io.github.sinri.keel.app.cli;

import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;

/**
 * 支持解析类似以下的命令行参数：
 * <p>
 * 混合格式：选项和标志在前，然后如果需要，使用 {@code --} 标记（必需）和参数。 <br>
 * {@code java -jar my-app.jar
 * --long-option-name long-option-value -s short-option-value --flag-name -f -- Parameter1 Parameter2}
 * <p>
 * 纯参数格式：无选项或标志，只有参数，{@code --} 标记不是必需的。<br>
 * {@code java -jar my-app.jar Parameter1 Parameter2}
 * <p>
 * 定义：<br>
 * 选项：带有短名称或长名称的标志，后跟一个值，例如 {@code --option1 value1}。<br>
 * 标志：带有短名称或长名称的标志，例如 {@code -f} 或 {@code --no-output}。<br>
 * 参数：不是选项或标志的参数，没有命名标签但有索引，通常在开头或在 {@code --} 之后，例如 {@code Parameter1} 或 {@code  -- Parameter2}。
 * @since 5.0.0
 */
public interface KeelCliArgsParser {
    static KeelCliArgsParser create() {
        return new KeelCliArgsParserImpl();
    }

    /**
     * 解析给定的命令行参数数组，并返回一个包含解析选项、标志和参数的结果对象。
     *
     * @param args 要解析的命令行参数数组，来自 main 方法的 args 参数
     * @return 表示解析后的命令行选项、标志和参数的结果对象
     * @throws KeelCliArgsParseError 如果解析失败
     */
    @NotNull
    KeelCliArgs parse(String[] args) throws KeelCliArgsParseError;

    void addOption(@NotNull KeelCliOption option) throws KeelCliArgsDefinitionError;

    default void addOption(@NotNull Handler<KeelCliOption> optionHandler) throws KeelCliArgsDefinitionError {
        KeelCliOption option = new KeelCliOption();
        optionHandler.handle(option);
        addOption(option);
    }
}
