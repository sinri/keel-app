package io.github.sinri.keel.app.launcher;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.launcher.application.HookContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeelVertxApplicationHooksUnitTest {
    private static final String ROOT_KEY = "keelVertxApplicationHooksUnitTest";

    @AfterEach
    void removeTestConfig() {
        ConfigElement.root().removeChild(ROOT_KEY);
    }

    @Test
    void convertsNestedObjectsAndPreservesScalarStringSemantics() throws Exception {
        var config = new JsonObject().put(ROOT_KEY, new JsonObject()
                .put("nested", new JsonObject().put("value", "text"))
                .put("string", "hello")
                .put("number", 42)
                .put("boolean", true)
                .putNull("null")
                .put("array", new JsonArray().add("item").add(7)));

        new TestHooks().afterConfigParsed(config);

        var converted = ConfigElement.root().getChild(ROOT_KEY);
        assertEquals("text", converted.extract("nested", "value").getElementValue());
        assertEquals("hello", converted.getChild("string").getElementValue());
        assertEquals("42", converted.getChild("number").getElementValue());
        assertEquals("true", converted.getChild("boolean").getElementValue());
        assertEquals("", converted.getChild("null").getElementValue());
        assertEquals("[\"item\",7]", converted.getChild("array").getElementValue());
    }

    private static final class TestHooks implements KeelVertxApplicationHooks {
        @Override
        public void afterVertxStarted(HookContext context) {
        }
    }
}
