package io.github.sinri.keel.app.cli;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;

/**
 * 注册时固化的选项定义，由解析器和解析结果共同使用。
 * <p>
 * 验证器保存函数引用，不复制函数捕获的外部状态。
 */
@NullMarked
record CommandLineOptionSnapshot(
        String id,
        Set<String> aliasSet,
        @Nullable String description,
        boolean isFlag,
        @Nullable Function<String, Boolean> valueValidator
) {
    CommandLineOptionSnapshot {
        aliasSet = Set.copyOf(aliasSet);
    }

    static CommandLineOptionSnapshot from(CommandLineOption option) {
        return new CommandLineOptionSnapshot(
                option.id(), option.getAliasSet(), option.description(),
                option.isFlag(), option.getValueValidator());
    }
}
