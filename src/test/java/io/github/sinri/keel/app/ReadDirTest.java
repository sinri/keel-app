package io.github.sinri.keel.app;

import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;

public class ReadDirTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        return getKeel().fileSystem().readDir("./")
                        .compose(list -> {
                            list.forEach(System.out::println);
                            return Future.succeededFuture();
                        });
    }
}
