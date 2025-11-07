package io.github.sinri.keel.facade.cli;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;

/**
 * Support to parse command-line arguments like:
 * <p>
 * Mixed Format: options and flags ahead, then, if needed, a {@code --} mark (which is required) and parameters. <br>
 * {@code java -jar my-app.jar
 * --long-option-name long-option-value -s short-option-value --flag-name -f -- Parameter1 Parameter2}
 * <p>
 * Parameter-Only Format: no options nor flags, parameters only, {@code --} mark is not required.<br>
 * {@code java -jar my-app.jar Parameter1 Parameter2}
 * <p>
 * Definitions:<br>
 * Option: a flag with a short name or a long name, and followed by a value, e.g. {@code --option1 value1}.<br>
 * Flag: a flag with a short name or a long name, e.g. {@code -f} or {@code --no-output}.<br>
 * Parameter: a parameter that is not an option or flag, without named label but indexed,
 * usually on the head or at the tail after {@code --}, e.g. {@code Parameter1} or {@code  -- Parameter2}.
 *
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public interface KeelCliArgsParser {
    static KeelCliArgsParser create() {
        return new KeelCliArgsParserImpl();
    }

    /**
     * Parses the given array of command-line arguments and returns a result
     * containing the parsed options, flags, and arguments.
     *
     * @param args the array of command-line arguments to parse, from a main method's args parameter
     * @return a result object representing the parsed command-line options, flags, and arguments
     * @throws KeelCliArgsParseError if the parsing fails
     */
    @Nonnull
    KeelCliArgs parse(String[] args) throws KeelCliArgsParseError;

    void addOption(@Nonnull KeelCliOption option) throws KeelCliArgsDefinitionError;

    default void addOption(@Nonnull Handler<KeelCliOption> optionHandler) throws KeelCliArgsDefinitionError {
        KeelCliOption option = new KeelCliOption();
        optionHandler.handle(option);
        addOption(option);
    }
}
