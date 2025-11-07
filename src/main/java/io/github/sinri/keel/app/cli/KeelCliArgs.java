package io.github.sinri.keel.facade.cli;


import io.github.sinri.keel.base.annotations.TechnicalPreview;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the results of parsing command-line arguments. It provides methods
 * to retrieve options, flags, and positional parameters from the parsed input.
 * <p>
 * This interface is marked as a technical preview and may be subject to changes.
 */
@TechnicalPreview(since = "4.1.1")
public interface KeelCliArgs {

    /**
     * Retrieves the value of an option specified by its short name,
     * delegating to the {@link #readOption(String)} method.
     *
     * @param shortName the single-character name of the option to retrieve
     * @return if the option is not provided, return {@code null};
     *         if the option is provided and as a flag, return {@code ""};
     *         otherwise return the provided value for the option.
     */
    @Nullable
    default String readOption(char shortName) {
        return readOption(String.valueOf(shortName));
    }

    /**
     * Retrieves the value of an option specified by its long name.
     *
     * @param longName the full name of the option to retrieve; must not be null
     * @return if the option is not provided, return {@code null};
     *         if the option is provided and as a flag, return {@code ""};
     *         otherwise return the provided value to the option.
     */
    @Nullable
    String readOption(@Nonnull String longName);

    /**
     * Checks if an option represented by a short name is present in the parsed command-line arguments.
     * <p>
     * Note, an option is also treated as a flag in this method.
     *
     * @param shortName the single-character name of the flag to check
     * @return true if the specified flag is present; false otherwise
     */
    default boolean readFlag(char shortName) {
        return readFlag(String.valueOf(shortName));
    }

    /**
     * Checks if an option represented by a long name is present in the parsed command-line arguments.
     * <p>
     * Note, an option is also treated as a flag in this method.
     *
     * @param longName the full name of the flag to check; must not be null
     * @return true if the specified flag is present; false otherwise
     */
    boolean readFlag(@Nonnull String longName);

    /**
     * Retrieves the value of a positional parameter by index.
     * <p>
     * The index of a parameter is not always the index as the raw argument,
     * for the mixed format with options, the index is the position after {@code --}.
     *
     * @param index the zero-based index of the positional parameter to retrieve
     * @return the value of the positional parameter at the given index,
     *         or null if the parameter is not present
     */
    @Nullable
    String readParameter(int index);
}
