package io.github.sinri.keel.app.cli;


/**
 * 在解析命令行之前进行参数定义的过程里可能出现的异常。
 * <p>
 * 预期抛出原因：重复定义选项、选项名称为空等。
 *
 * @since 5.0.0
 */
public class CommandLineArgumentsDefinitionError extends RuntimeException {
    public CommandLineArgumentsDefinitionError(String msg) {
        super(msg);
    }
}
