package io.github.sinri.keel.facade.cli;

import io.github.sinri.keel.base.annotations.TechnicalPreview;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@TechnicalPreview(since = "4.1.1")
class KeelCliArgsImpl implements KeelCliArgs, KeelCliArgsWriter {
    /**
     * Map option id to option.
     */
    private final Map<String, KeelCliOption> idToOptionMap = new HashMap<>();
    /**
     * Map long name to option id.
     */
    private final Map<String, String> nameToOptionIdMap = new HashMap<>();
    /**
     * Map option id to option value (if the option is a flag, the value is null).
     */
    private final Map<String, String> idToOptionValueMap = new HashMap<>();
    /**
     * List of parameters.
     */
    private final List<String> parameters = new ArrayList<>();

    public KeelCliArgsImpl() {
    }

    public void recordOption(@Nonnull KeelCliOption option, @Nullable String value) {
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
    public String readOption(@Nonnull String longName) {
        String optionId = nameToOptionIdMap.get(longName);
        if (optionId == null) return null;
        KeelCliOption option = idToOptionMap.get(optionId);
        if (option == null) return null;
        if (option.isFlag()) return "";
        return idToOptionValueMap.get(optionId);
    }

    @Override
    public boolean readFlag(@Nonnull String longName) {
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
    public void recordParameter(@Nonnull String parameter) {
        parameters.add(parameter);
    }

    @Override
    public KeelCliArgs toResult() {
        return this;
    }
}
