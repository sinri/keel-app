package io.github.sinri.keel.app.cli;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * KeelCliArgsParser 接口的实现类。
 * 该类负责实际的命令行参数解析逻辑。
 *
 * @since 5.0.0
 */
class CommandLineArgumentsParserImpl implements CommandLineArgumentsParser {
    private final Map<String, CommandLineOption> optionMap = new HashMap<>();
    private final Map<String, String> nameToOptionIdMap = new HashMap<>();

    public void addOption(@NotNull CommandLineOption option) throws CommandLineArgumentsDefinitionError {
        if (optionMap.containsKey(option.id())) {
            throw new CommandLineArgumentsDefinitionError("Duplicate named argument definition id: " + option.id());
        }
        optionMap.put(option.id(), option);

        Set<String> aliasSet = option.getAliasSet();
        if (aliasSet.isEmpty()) {
            throw new CommandLineArgumentsDefinitionError("Option must have at least one alias");
        }

        for (var alias : aliasSet) {
            // believe the members in the alias set is all valid
            if (nameToOptionIdMap.containsKey(alias)) {
                throw new CommandLineArgumentsDefinitionError("Alias cannot duplicate: " + alias);
            }
            nameToOptionIdMap.put(alias, option.id());
        }
    }

    @NotNull
    @Override
    public CommandLineArguments parse(String[] args) throws CommandLineArgumentsParseError {
        var parsedResult = CommandLineArgumentsWriter.create();

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
        CommandLineOption currentOption = null;
        for (String arg : args) {
            if (arg == null)
                continue;
            if (mode == 0 || mode == 2) {
                if ("--".equals(arg)) {
                    mode = 3;
                } else {
                    String parsedOptionName = CommandLineOption.parseOptionName(arg);
                    if (parsedOptionName == null) {
                        if (mode == 0) {
                            // arg is a parameter
                            parameters.add(arg);
                            mode = 3;
                        } else {
                            throw new CommandLineArgumentsParseError("Invalid option: " + arg);
                        }
                    } else {
                        String optionId = nameToOptionIdMap.get(parsedOptionName);
                        if (optionId == null) {
                            throw new CommandLineArgumentsParseError("Option " + parsedOptionName + " not found");
                        }
                        currentOption = optionMap.get(optionId);
                        if (currentOption == null) {
                            throw new CommandLineArgumentsParseError("Option " + parsedOptionName + " not found");
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
                    throw new CommandLineArgumentsParseError("Invalid option: " + arg);
                }
                options.put(currentOption.id(), arg);
                mode = 2;
                currentOption = null;
            } else if (mode == 3) {
                parameters.add(arg);
            }
        }

        if (mode == 1 && currentOption != null) {
            throw new CommandLineArgumentsParseError("Invalid option: " + currentOption);
        }

        for (var entry : options.entrySet()) {
            String optionId = entry.getKey();
            String optionValue = entry.getValue();
            CommandLineOption option = optionMap.get(optionId);
            Function<String, Boolean> valueValidator = option.getValueValidator();
            if (valueValidator != null && !option.isFlag()) {
                boolean valueValid = valueValidator.apply(optionValue);
                if (!valueValid) {
                    throw new CommandLineArgumentsParseError(
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
