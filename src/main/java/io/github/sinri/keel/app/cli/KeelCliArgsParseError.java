package io.github.sinri.keel.app.cli;


import io.github.sinri.keel.base.annotations.TechnicalPreview;

/**
 * An exception that represents an error occurring during the parsing of command-line arguments
 * by a {@code CommandLineParser}.
 * <p>
 * This exception is typically thrown when the command-line parsing process encounters invalid input,
 * such as improperly formatted options, unknown flags, missing required parameters, or other parsing-related issues.
 *
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public class KeelCliArgsParseError extends Exception {
    public KeelCliArgsParseError(String message) {
        super(message);
    }
}
