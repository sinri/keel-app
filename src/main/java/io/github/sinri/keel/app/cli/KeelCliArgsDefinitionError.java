package io.github.sinri.keel.app.cli;


import io.github.sinri.keel.base.annotations.TechnicalPreview;

/**
 * An exception defined for CommandLineParser preparation before parsing the command line.
 * <p>
 * Expected thrown causes: duplicated options defining, null option naming, etc.
 *
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public class KeelCliArgsDefinitionError extends RuntimeException {
    public KeelCliArgsDefinitionError(String msg) {
        super(msg);
    }
}
