package io.github.sinri.keel.app.runner;

import io.github.sinri.keel.app.cli.CommandLineArgumentsParser;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProgramContextInitializationUnitTest {

    @Test
    void contextIsNotBuiltBeforeSubclassConstructionCompletes() {
        var program = new TestProgram("configured-value");

        assertEquals(0, program.contextBuildCount);
        assertThrows(IllegalStateException.class, program::getProgramContext);
    }

    private static final class TestContext extends AbstractProgramContext {
        private final String value;

        private TestContext(String value) {
            this.value = value;
        }
    }

    private static final class TestProgram extends Program<TestContext> {
        private final String configuredValue;
        private int contextBuildCount;

        private TestProgram(String configuredValue) {
            this.configuredValue = configuredValue;
        }

        @Override
        protected TestContext buildProgramContext() {
            contextBuildCount++;
            return new TestContext(configuredValue);
        }

        @Override
        protected @Nullable CommandLineArgumentsParser buildCommandLineParser() {
            return null;
        }

        @Override
        protected Future<Void> launchAsProgram() {
            return Future.succeededFuture();
        }
    }
}
