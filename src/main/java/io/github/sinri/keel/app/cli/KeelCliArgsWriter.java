package io.github.sinri.keel.facade.cli;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

interface KeelCliArgsWriter {
    static KeelCliArgsWriter create() {
        return new KeelCliArgsImpl();
    }

    void recordParameter(@Nonnull String parameter);

    void recordOption(@Nonnull KeelCliOption option, @Nullable String value);

    KeelCliArgs toResult();
}
