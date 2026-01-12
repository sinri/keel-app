module keel.app.main {
    requires transitive io.github.sinri.keel.base;
    requires transitive io.github.sinri.keel.core;
    requires transitive io.github.sinri.keel.logger.api;
    requires transitive io.github.sinri.keel.web;

    requires transitive io.vertx.core;
    requires transitive io.vertx.core.logging;
    requires transitive io.vertx.config;
    requires transitive io.vertx.launcher.application;
    requires transitive io.vertx.auth.common;
    requires transitive io.vertx.web;
    requires transitive io.vertx.web.client;
    requires transitive io.vertx.sql.client;
    requires transitive io.vertx.sql.client.mysql;
    requires transitive io.vertx.mail.client;

    requires transitive org.commonmark;
    requires transitive org.commonmark.ext.gfm.tables;

    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;

    requires static org.jspecify;
    requires io.github.sinri.keel.integration.aliyun;
    requires java.desktop;

    exports io.github.sinri.keel.app.cli;
    exports io.github.sinri.keel.app.common;
    exports io.github.sinri.keel.app.common.monitor;
    exports io.github.sinri.keel.app.launcher;
    exports io.github.sinri.keel.app.runner;
    exports io.github.sinri.keel.app.runner.service;
}