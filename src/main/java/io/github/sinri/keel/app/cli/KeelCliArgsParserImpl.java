package io.github.sinri.keel.facade.cli;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

class KeelCliArgsParserImpl implements KeelCliArgsParser {
    private final Map<String, KeelCliOption> optionMap = new HashMap<>();
    private final Map<String, String> nameToOptionIdMap = new HashMap<>();

    public void addOption(@Nonnull KeelCliOption option) throws KeelCliArgsDefinitionError {
        if (optionMap.containsKey(option.id())) {
            throw new KeelCliArgsDefinitionError("Duplicate named argument definition id: " + option.id());
        }
        optionMap.put(option.id(), option);

        Set<String> aliasSet = option.getAliasSet();
        if (aliasSet.isEmpty()) {
            throw new KeelCliArgsDefinitionError("Option must have at least one alias");
        }

        for (var alias : aliasSet) {
            // believe the members in the alias set is all valid
            if (nameToOptionIdMap.containsKey(alias)) {
                throw new KeelCliArgsDefinitionError("Alias cannot duplicate: " + alias);
            }
            nameToOptionIdMap.put(alias, option.id());
        }
    }

    @Nonnull
    @Override
    public KeelCliArgs parse(String[] args) throws KeelCliArgsParseError {
        var parsedResult = KeelCliArgsWriter.create();

        if (args == null || args.length == 0) {
            return parsedResult.toResult();
        }

        Map<String, String> options = new TreeMap<>();
        List<String> parameters = new ArrayList<>();

        /*
         * mode=0: before options and parameters
         * mode=1: met option name, start option
         * mode=2: met option value, or confirmed flag, end option
         * mode=3: met -- or parameter
         */

        int mode = 0;
        KeelCliOption currentOption = null;
        for (String arg : args) {
            if (arg == null)
                continue;
            if (mode == 0 || mode == 2) {
                if ("--".equals(arg)) {
                    mode = 3;
                } else {
                    String parsedOptionName = KeelCliOption.parseOptionName(arg);
                    if (parsedOptionName == null) {
                        if (mode == 0) {
                            // arg is a parameter
                            parameters.add(arg);
                            mode = 3;
                        } else {
                            throw new KeelCliArgsParseError("Invalid option: " + arg);
                        }
                    } else {
                        String optionId = nameToOptionIdMap.get(parsedOptionName);
                        if (optionId == null) {
                            throw new KeelCliArgsParseError("Option " + parsedOptionName + " not found");
                        }
                        currentOption = optionMap.get(optionId);
                        if (currentOption == null) {
                            throw new KeelCliArgsParseError("Option " + parsedOptionName + " not found");
                        }
                        if (currentOption.isFlag()) {
                            options.put(currentOption.id(), null);
                            currentOption = null;
                            mode = 2;
                        } else {
                            mode = 1;
                        }
                    }
                }
            } else if (mode == 1) {
                if (currentOption == null) {
                    throw new KeelCliArgsParseError("Invalid option: " + arg);
                }
                options.put(currentOption.id(), arg);
                mode = 2;
                currentOption = null;
            } else if (mode == 3) {
                parameters.add(arg);
            }
        }

        if (mode == 1 && currentOption != null) {
            throw new KeelCliArgsParseError("Invalid option: " + currentOption);
        }

        for (var entry : options.entrySet()) {
            String optionId = entry.getKey();
            String optionValue = entry.getValue();
            KeelCliOption option = optionMap.get(optionId);
            Function<String, Boolean> valueValidator = option.getValueValidator();
            if (valueValidator != null && !option.isFlag()) {
                boolean valueValid = valueValidator.apply(optionValue);
                if (!valueValid) {
                    throw new KeelCliArgsParseError(
                            "Value for option " + (String.join("/", option.getAliasSet())) + " is not valid.");
                }
            }
            // System.out.printf("option[%s]: %s\n", optionId, option);
            parsedResult.recordOption(option, optionValue);
        }
        for (String p : parameters) {
            // System.out.printf("parameter: %s\n", p);
            parsedResult.recordParameter(p);
        }

        return parsedResult.toResult();
    }
}
