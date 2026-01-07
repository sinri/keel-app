package io.github.sinri.keel.app.cli;


import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * KeelCliArgs 和 KeelCliArgsWriter 接口的实现类。
 * 该类负责存储和操作解析后的命令行参数数据。
 *
 * @since 5.0.0
 */
@NullMarked
class CommandLineArgumentsImpl implements CommandLineArguments, CommandLineArgumentsWriter {
    /**
     * 将选项ID映射到选项对象的映射表。
     */
    private final Map<String, CommandLineOption> idToOptionMap = new HashMap<>();
    /**
     * 将选项长名称映射到选项ID的映射表。
     */
    private final Map<String, String> nameToOptionIdMap = new HashMap<>();
    /**
     * 将选项ID映射到选项值的映射表（如果选项是标志，则值为null）。
     */
    private final Map<String, @Nullable String> idToOptionValueMap = new HashMap<>();
    /**
     * 位置参数列表。
     */
    private final List<String> parameters = new ArrayList<>();

    public CommandLineArgumentsImpl() {
    }

    public void recordOption(CommandLineOption option, @Nullable String value) {
        idToOptionMap.put(option.id(), option);
        Set<String> aliasSet = option.getAliasSet();
        for (var alias : aliasSet) {
            if (alias.isEmpty()) continue;
            nameToOptionIdMap.put(alias, option.id());
        }
        idToOptionValueMap.put(option.id(), value);
    }

    @Nullable
    @Override
    public String readOption(String longName) {
        String optionId = nameToOptionIdMap.get(longName);
        if (optionId == null) return null;
        CommandLineOption option = idToOptionMap.get(optionId);
        if (option == null) return null;
        if (option.isFlag()) return "";
        return idToOptionValueMap.get(optionId);
    }

    @Override
    public boolean readFlag(String longName) {
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
    public void recordParameter(String parameter) {
        parameters.add(parameter);
    }

    @Override
    public CommandLineArguments toResult() {
        return this;
    }
}
