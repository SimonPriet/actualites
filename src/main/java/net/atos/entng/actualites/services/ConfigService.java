package net.atos.entng.actualites.services;

import io.vertx.core.json.JsonObject;

public class ConfigService {
    private static final ConfigService INSTANCE = new ConfigService();
    private JsonObject shareConfig;

    private ConfigService() {
    }

    public static ConfigService getInstance() {
        return ConfigService.INSTANCE;
    }

    public JsonObject getShareConfig() {
        return shareConfig;
    }

    public void setShareConfig(JsonObject shareConfig) {
        this.shareConfig = shareConfig;
    }
}
