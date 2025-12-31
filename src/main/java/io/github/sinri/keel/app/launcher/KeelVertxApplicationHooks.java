package io.github.sinri.keel.app.launcher;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.vertx.core.json.JsonObject;
import io.vertx.launcher.application.HookContext;
import io.vertx.launcher.application.VertxApplication;
import io.vertx.launcher.application.VertxApplicationHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * 如果你使用 Vert.x Application Launcher，你可以使用这个 hooks 扩展接口来基于 vertx 和配置构建 {@link Keel} 实例。
 *
 * @see <a href="https://vertx.io/docs/vertx-launcher-application/java/">Vert.x Application Launcher</a>
 * @since 5.0.0
 */
public interface KeelVertxApplicationHooks extends VertxApplicationHooks {
    @NotNull
    private static List<ConfigElement> transformJsonObjectToConfigElements(@NotNull JsonObject jsonObject) {
        List<ConfigElement> list = new ArrayList<>();

        jsonObject.forEach(entry -> {
            String key = entry.getKey();
            var child = new ConfigElement(key);
            try {
                var r = jsonObject.getJsonObject(key);
                transformJsonObjectToConfigElement(r, child);
            } catch (Throwable throwable) {
                Object value = entry.getValue();
                if (value != null) {
                    child.setElementValue(value.toString());
                }
            }
            list.add(child);
        });

        return list;
    }

    private static void transformJsonObjectToConfigElement(@NotNull JsonObject jsonObject, @NotNull ConfigElement configElement) {
        jsonObject.forEach(entry -> {
            String key = entry.getKey();
            var child = configElement.ensureChild(key);
            try {
                var r = jsonObject.getJsonObject(key);
                transformJsonObjectToConfigElement(r, child);
            } catch (Throwable throwable) {
                Object value = entry.getValue();
                if (value != null) {
                    child.setElementValue(value.toString());
                }
            }
        });
    }

    @NotNull
    Keel getKeel();

    /**
     * 以给定的参数中可以获取到的 vertx 构建一个 Keel 实例。
     *
     * @param context exposes objects available at this stage of the {@link VertxApplication} launch process
     */
    @Override
    void afterVertxStarted(HookContext context);

    @Override
    default JsonObject afterConfigParsed(JsonObject config) {
        var configElements = transformJsonObjectToConfigElements(config);
        configElements.forEach(configElement -> getKeel().getConfiguration().addChild(configElement));
        return VertxApplicationHooks.super.afterConfigParsed(config);
    }
}
