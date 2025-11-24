package io.github.sinri.keel.app.cli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * KeelCliArgs 和 KeelCliArgsWriter 接口的实现类。
 * 该类负责存储和操作解析后的命令行参数数据。
 *
 * @since 5.0.0
 */
class KeelCliArgsImpl implements KeelCliArgs, KeelCliArgsWriter {
    /**
     * 将选项ID映射到选项对象的映射表。
     */
    private final Map<String, KeelCliOption> idToOptionMap = new HashMap<>();
    /**
     * 将选项长名称映射到选项ID的映射表。
     */
    private final Map<String, String> nameToOptionIdMap = new HashMap<>();
    /**
     * 将选项ID映射到选项值的映射表（如果选项是标志，则值为null）。
     */
    private final Map<String, String> idToOptionValueMap = new HashMap<>();
    /**
     * 位置参数列表。
     */
    private final List<String> parameters = new ArrayList<>();

    public KeelCliArgsImpl() {
    }

    public void recordOption(@NotNull KeelCliOption option, @Nullable String value) {
        idToOptionMap.put(option.id(), option);
        Set<String> aliasSet = option.getAliasSet();
        for (var alias : aliasSet) {
            if (alias == null || alias.isEmpty()) continue;
            nameToOptionIdMap.put(alias, option.id());
        }
        idToOptionValueMap.put(option.id(), value);
    }

    @Nullable
    @Override
    public String readOption(@NotNull String longName) {
        String optionId = nameToOptionIdMap.get(longName);
        if (optionId == null) return null;
        KeelCliOption option = idToOptionMap.get(optionId);
        if (option == null) return null;
        if (option.isFlag()) return "";
        return idToOptionValueMap.get(optionId);
    }

    @Override
    public boolean readFlag(@NotNull String longName) {
        return readOption(longName) != null;
    }

    @Nullable
    @Override
    public String readParameter(int index) {
        if (index < 0 || index >= parameters.size()) {
            return null;
        }
        return parameters.get(index);
    }

    @Override
    public void recordParameter(@NotNull String parameter) {
        parameters.add(parameter);
    }

    @Override
    public KeelCliArgs toResult() {
        return this;
    }
}
