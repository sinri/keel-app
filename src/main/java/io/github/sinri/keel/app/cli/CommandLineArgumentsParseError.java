package io.github.sinri.keel.app.cli;


/**
 * 在解析命令行参数期间发生的错误的异常。
 * <p>
 * 当命令行解析过程遇到无效输入时，通常会抛出此异常，例如格式不正确的选项、未知标志、缺少必需参数或其他解析相关问题。
 *
 * @since 5.0.0
 */
public class CommandLineArgumentsParseError extends Exception {
    public CommandLineArgumentsParseError(String message) {
        super(message);
    }
}
