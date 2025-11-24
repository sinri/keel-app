package io.github.sinri.keel.app.cli;


/**
 * 为在解析命令行之前进行 CommandLineParser 准备而定义的异常。
 * <p>
 * 预期抛出原因：重复定义选项、选项名称为空等。
 * @since 5.0.0
 */
public class KeelCliArgsDefinitionError extends RuntimeException {
    public KeelCliArgsDefinitionError(String msg) {
        super(msg);
    }
}
