package io.github.sinri.keel.app.cli;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 表示CLI应用程序的命令行选项或标志。该类支持定义带有ID、别名、描述、值的选项，
 * 以及可选的验证机制。KeelCliOption 还支持标志，这些是没有值的布尔选项。
 * <p>
 * 该类被标记为技术预览版本，其API可能会在未来的版本中发生变化。
 * <p>
 * 功能特性：<br>
 * - 支持短选项（例如 `-o`）和长选项（例如 `--option`）。<br>
 * - 允许为单个选项定义多个别名。<br>
 * - 提供针对预定义模式的别名验证。<br>
 * - 支持为CLI选项添加可选描述。<br>
 - 允许将选项配置为标志（布尔值，无关联值）。<br>
 * - 提供可选的值验证器来动态验证输入值。<br>
 * - 通过不可变的别名集保护内部状态。<br>
 * @since 5.0.0
 */
@TechnicalPreview(since = "4.1.1")
public class KeelCliOption {
    private final static Pattern VALID_ALIAS_PATTERN = Pattern.compile("^[A-Za-z0-9_.][A-Za-z0-9_.-]*$");
    private final static Pattern VALID_SHORT_PATTERN = Pattern.compile("^-[A-Za-z0-9_]$");
    private final static Pattern VALID_LONG_PATTERN = Pattern.compile("^--[A-Za-z0-9_.][A-Za-z0-9_.-]*$");
    @NotNull
    private final String id;
    @NotNull
    private final Set<String> aliasSet;
    @Nullable
    private String description;
    private boolean flag;
    @Nullable
    private Function<String, Boolean> valueValidator;

    /**
     * KeelCliOption 类的默认构造函数。
     * 该构造函数用唯一标识符和空别名集初始化 KeelCliOption 类的新实例。
     */
    public KeelCliOption() {
        this.id = UUID.randomUUID().toString();
        this.aliasSet = new HashSet<>();
    }

    /**
     * 从给定的参数字符串中解析选项名称。
     * 该方法检查参数是否匹配长选项或短选项模式，并提取相应的选项名称（不包含前面的破折号）。
     * <p>
     * 提供的参数字符串不能为null，除该方法外不应处理 {@code --}。
     *
     * @param argument 要解析的命令行参数字符串；不能为null
     * @return 如果参数匹配选项格式则返回提取的选项名称，否则返回null
     */
    @Nullable
    static String parseOptionName(@NotNull String argument) {
        if ("--".equals(argument)) return null;
        if (argument.startsWith("--")) {
            if (VALID_LONG_PATTERN.matcher(argument).matches()) {
                return argument.substring(2);
            }
        }
        if (argument.startsWith("-")) {
            if (VALID_SHORT_PATTERN.matcher(argument).matches()) {
                return argument.substring(1);
            }
        }
        return null;
    }

    /**
     * 针对预定义模式验证给定的别名字符串。
     * 别名不能为null，必须匹配定义的别名模式。
     * 如果验证失败，抛出 IllegalArgumentException。
     *
     * @param alias 要验证的别名字符串；不能为null且必须匹配别名模式
     * @throws IllegalArgumentException 如果别名为null或不匹配别名模式
     */
    public static String validatedAlias(String alias) {
        if (alias == null || !VALID_ALIAS_PATTERN.matcher(alias).matches()) {
            throw new IllegalArgumentException("Alias cannot be null");
        }
        return alias;
    }

    /**
     * 获取此选项的唯一标识符。
     * 该标识符是自动生成的，不是由用户定义的。
     *
     * @return 此选项的字符串标识符
     */
    public String id() {
        return id;
    }

    /**
     * 获取此命令行选项的描述。
     *
     * @return 选项的字符串描述
     */
    public String description() {
        return description;
    }

    /**
     * 为此命令行选项设置描述。
     *
     * @param description 命令行选项的描述
     * @return 当前 {@code KeelCliOption} 实例，用于方法链式调用
     */
    public KeelCliOption description(String description) {
        this.description = description;
        return this;
    }

    /**
     * 检查当前命令行选项是否为标志。
     * 标志代表没有值的选项，通常用作布尔开关。
     *
     * @return 如果选项是标志返回 {@code true}，否则返回 {@code false}
     */
    public boolean isFlag() {
        return flag;
    }

    /**
     * 将当前命令行选项标记为标志。
     * 标志代表没有值的选项，通常用作布尔开关。
     * 该方法将内部标志状态设置为true，并启用方法链式调用。
     *
     * @return 当前 {@code KeelCliOption} 实例，用于方法链式调用
     */
    public KeelCliOption flag() {
        this.flag = true;
        return this;
    }

    /**
     * 获取与此命令行选项关联的值验证器函数。
     * 值验证器是一个函数，以字符串作为输入，返回布尔值指示提供的值是否对此选项有效。
     *
     * @return 用于验证选项值的函数，如果未设置验证器则返回 {@code null}
     */
    @Nullable
    public Function<String, Boolean> getValueValidator() {
        return valueValidator;
    }

    /**
     * 为此命令行选项设置值验证器函数。
     * 值验证器是一个函数，以字符串作为输入，返回布尔值指示提供的值是否对此选项有效。
     *
     * @param valueValidator 用于验证选项值的函数，如果不需要验证则可以设置为 {@code null}
     * @return 当前 {@code KeelCliOption} 实例，用于方法链式调用
     */
    public KeelCliOption setValueValidator(@Nullable Function<String, Boolean> valueValidator) {
        this.valueValidator = valueValidator;
        return this;
    }

    /**
     * 为当前命令行选项添加别名。
     * 别名必须有效，根据预定义的别名模式，在添加到别名集之前会进行验证。
     *
     * @param alias 要添加的别名字符串；不能为null且必须匹配别名模式
     * @return 当前 {@code KeelCliOption} 实例，用于方法链式调用
     * @throws KeelCliArgsDefinitionError 如果别名为null或不匹配别名模式
     */
    public KeelCliOption alias(@NotNull String alias) throws KeelCliArgsDefinitionError {
        try {
            this.aliasSet.add(validatedAlias(alias));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new KeelCliArgsDefinitionError(illegalArgumentException.getMessage());
        }
        return this;
    }

    /**
     * 获取与此命令行选项关联的别名集。
     * 返回的集合是不可变的，反映了别名的当前状态。
     *
     * @return 与此选项关联的不可变别名字符串集合
     */
    public Set<String> getAliasSet() {
        return Collections.unmodifiableSet(aliasSet);
    }
}
