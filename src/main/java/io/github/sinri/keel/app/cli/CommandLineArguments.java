package io.github.sinri.keel.app.cli;


import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 表示命令行参数解析结果的接口。提供了从解析输入中检索选项、标志和位置参数的方法。
 *
 * @since 5.0.0
 */
@NullMarked
public interface CommandLineArguments {

    /**
     * 根据短名称检索选项的值，委托给 {@link #readOption(String)} 方法。
     *
     * @param shortName 要检索的选项的单字符名称
     * @return 如果选项未提供，返回 {@code null}；
     *         如果选项作为标志提供，返回 {@code ""}；
     *         否则返回为选项提供的值。
     */
    default @Nullable String readOption(char shortName) {
        return readOption(String.valueOf(shortName));
    }

    /**
     * 根据长名称检索选项的值。
     *
     * @param longName 要检索的选项的完整名称；不能为null
     * @return 如果选项未提供，返回 {@code null}；
     *         如果选项作为标志提供，返回 {@code ""}；
     *         否则返回为选项提供的值。
     */
    @Nullable String readOption(String longName);

    /**
     * 检查解析后的命令行参数中是否存在由短名称表示的选项。
     * <p>
     * 注意，在此方法中选项也被视为标志。
     *
     * @param shortName 要检查的标志的单字符名称
     * @return 如果指定的标志存在返回true；否则返回false
     */
    default boolean readFlag(char shortName) {
        return readFlag(String.valueOf(shortName));
    }

    /**
     * 检查解析后的命令行参数中是否存在由长名称表示的选项。
     * <p>
     * 注意，在此方法中选项也被视为标志。
     *
     * @param longName 要检查的标志的完整名称；不能为null
     * @return 如果指定的标志存在返回true；否则返回false
     */
    boolean readFlag(String longName);

    /**
     * 根据索引检索位置参数的值。
     * <p>
     * 参数的索引并不总是与原始参数的索引相同，
     * 对于带有选项的混合格式，索引是 {@code --} 之后的位置。
     *
     * @param index 要检索的位置参数的零基索引
     * @return 给定索引处位置参数的值，如果参数不存在则返回null
     */
    @Nullable String readParameter(int index);
}
