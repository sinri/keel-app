package io.github.sinri.keel.app.common.monitor;

import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

/**
 * 以日志形式记录的监控信息。
 *
 * @since 5.0.0
 */
@NullMarked
public class MonitorLog extends SpecificLog<MonitorLog> {
    public static final String TopicHealthMonitor = "HealthMonitor";
    public static final String ExtraSnapshot = "snapshot";

    public MonitorLog() {
        super();
    }


    public MonitorLog snapshot(JsonObject snapshot) {
        this.extra(ExtraSnapshot, snapshot);
        return this;
    }
}
