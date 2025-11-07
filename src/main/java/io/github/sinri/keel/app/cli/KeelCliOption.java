package io.github.sinri.keel.facade.cli;

import io.github.sinri.keel.base.annotations.TechnicalPreview;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Represents a command-line option or flag for a CLI application. This class enables defining
 * options with an ID, aliases, description, a value, and an optional validation mechanism.
 * The KeelCliOption also supports flags, which are boolean options without values.
 * <p>
 * This class is marked as a technical preview, and its APIs are subject to change in future releases.
 * <p>
 * Features: <br>
 * - Supports short (e.g., `-o`) and long (e.g., `--option`) options. <br>
 * - Allows defining multiple aliases for a single option. <br>
 * - Provides validation for aliases against a predefined pattern. <br>
 * - Supports optional description for the CLI option. <br>
 * - Allows configuring the option as a flag (boolean without associated values). <br>
 * - Offers an optional value validator to validate input values dynamically. <br>
 * - Protects internal state with an immutable alias set. <br>
 *
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public class KeelCliOption {
    private final static Pattern VALID_ALIAS_PATTERN = Pattern.compile("^[A-Za-z0-9_.][A-Za-z0-9_.-]*$");
    private final static Pattern VALID_SHORT_PATTERN = Pattern.compile("^-[A-Za-z0-9_]$");
    private final static Pattern VALID_LONG_PATTERN = Pattern.compile("^--[A-Za-z0-9_.][A-Za-z0-9_.-]*$");
    @Nonnull
    private final String id;
    @Nonnull
    private final Set<String> aliasSet;
    @Nullable
    private String description;
    private boolean flag;
    @Nullable
    private Function<String, Boolean> valueValidator;

    /**
     * Default constructor for the `KeelCliOption` class.
     * This constructor initializes a new instance of the `KeelCliOption` class with a unique identifier
     * and an empty set of aliases.
     */
    public KeelCliOption() {
        this.id = UUID.randomUUID().toString();
        this.aliasSet = new HashSet<>();
    }

    /**
     * Parses the option name from a given argument string.
     * The method checks if the argument matches either a long or short option pattern
     * and extracts the corresponding option name without the leading dashes.
     * <p>
     * The provided argument string must not be null, and should process {@code --} other than this method.
     *
     * @param argument the command-line argument string to parse; must not be null
     * @return the extracted option name if the argument matches an option format, or null if it does not match
     */
    @Nullable
    static String parseOptionName(@Nonnull String argument) {
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
     * Validates the given alias string against a pre-defined pattern.
     * The alias cannot be null, and it must match the defined valid alias pattern.
     * If validation fails, an IllegalArgumentException is thrown.
     *
     * @param alias the alias string to validate; must not be null and must match the valid alias pattern
     * @throws IllegalArgumentException if the alias is null or does not match the valid alias pattern
     */
    public static String validatedAlias(String alias) {
        if (alias == null || !VALID_ALIAS_PATTERN.matcher(alias).matches()) {
            throw new IllegalArgumentException("Alias cannot be null");
        }
        return alias;
    }

    /**
     * Retrieves the unique identifier of this option.
     * The identifier is automatically generated, not defined by users.
     *
     * @return the identifier of this option as a string
     */
    public String id() {
        return id;
    }

    /**
     * Retrieves the description of this command-line option.
     *
     * @return the description of the option as a string
     */
    public String description() {
        return description;
    }

    /**
     * Sets the description for this command-line option.
     *
     * @param description the description of the command-line option
     * @return the current instance of {@code KeelCliOption} for method chaining
     */
    public KeelCliOption description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Checks if the current command-line option is a flag.
     * A flag represents an option with no value, typically used as a boolean switch.
     *
     * @return {@code true} if the option is a flag, {@code false} otherwise
     */
    public boolean isFlag() {
        return flag;
    }

    /**
     * Marks the current command-line option as a flag.
     * A flag represents an option with no value, typically used as a boolean switch.
     * This method sets the internal flag state to true and enables method chaining.
     *
     * @return the current instance of {@code KeelCliOption} for method chaining
     */
    public KeelCliOption flag() {
        this.flag = true;
        return this;
    }

    /**
     * Retrieves the value validator function associated with this command-line option.
     * The value validator is a function that takes a string as input and returns a boolean
     * indicating whether the provided value is valid for this option.
     *
     * @return a function for validating option values, or {@code null} if no validator is set
     */
    @Nullable
    public Function<String, Boolean> getValueValidator() {
        return valueValidator;
    }

    /**
     * Sets a value validator function for this command-line option.
     * The value validator is a function that takes a string input and returns a boolean
     * indicating whether the provided value is valid for this option.
     *
     * @param valueValidator the function to be used for validating the option's value,
     *                       or {@code null} if no validation is required
     * @return the current instance of {@code KeelCliOption} for method chaining
     */
    public KeelCliOption setValueValidator(@Nullable Function<String, Boolean> valueValidator) {
        this.valueValidator = valueValidator;
        return this;
    }

    /**
     * Adds an alias to the current command-line option.
     * The alias must be valid, according to the predefined alias pattern,
     * and it is validated before being added to the set of aliases.
     *
     * @param alias the alias string to add; must not be null and must match the valid alias pattern
     * @return the current instance of {@code KeelCliOption} for method chaining
     * @throws KeelCliArgsDefinitionError if the alias is null or does not match the valid alias pattern
     */
    public KeelCliOption alias(@Nonnull String alias) throws KeelCliArgsDefinitionError {
        try {
            this.aliasSet.add(validatedAlias(alias));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new KeelCliArgsDefinitionError(illegalArgumentException.getMessage());
        }
        return this;
    }

    /**
     * Retrieves the set of aliases associated with this command-line option.
     * The returned set is immutable and reflects the current state of aliases.
     *
     * @return an unmodifiable set of alias strings associated with this option
     */
    public Set<String> getAliasSet() {
        return Collections.unmodifiableSet(aliasSet);
    }
}
