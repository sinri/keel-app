module keel.app.main {
    requires transitive io.github.sinri.keel.base;
    requires transitive io.github.sinri.keel.core;
    requires transitive io.github.sinri.keel.logger.api;
    requires transitive io.github.sinri.keel.web;
    requires transitive io.vertx.core;
    requires transitive io.vertx.launcher.application;
    requires static org.jetbrains.annotations;

    exports io.github.sinri.keel.app.cli;
    exports io.github.sinri.keel.app.common;
    exports io.github.sinri.keel.app.common.monitor;
    exports io.github.sinri.keel.app.launcher;
    exports io.github.sinri.keel.app.runner;
    exports io.github.sinri.keel.app.runner.service;
}