package dev.syoritohatsuki.fstatsapi.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static dev.syoritohatsuki.fstatsapi.FStatsApi.MOD_ID;
import static dev.syoritohatsuki.fstatsapi.FStatsApi.logger;

public final class ConfigManager {

    private static final File configDir = Paths.get("", "config", MOD_ID).toFile();
    private static final File configFile = new File(configDir, "config.json");

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Config defaultConfig = new Config(1, true, false, new Config.Messages(true, true, true));

    public static void init() {
        if (!configDir.exists()) configDir.mkdirs();
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                Files.writeString(configFile.toPath(), gson.toJson(defaultConfig));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (!Objects.equals(read().getVersion(), defaultConfig.getVersion())) {
            try {
                logger.warn("Looks like config is deprecated... Updating...");

                var enabled = read().isEnabled() == null || read().isEnabled();
                var hideLocation = read().isLocationHide() != null && read().isLocationHide();
                var messages = read().getMessages() == null ? new Config.Messages(true, true, true) : read().getMessages();

                Files.writeString(configFile.toPath(), gson.toJson(new Config(defaultConfig.getVersion(), enabled, hideLocation, messages)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Config read() {
        try {
            return gson.fromJson(Files.readString(configFile.toPath()), Config.class);
        } catch (Exception e) {
            logger.error("Can't read config or it don't exist");
            try {
                logger.info("Backup config...");
                Files.copy(configFile.toPath(), new File(configDir, "backup_config.json").toPath());
                Files.writeString(configFile.toPath(), gson.toJson(defaultConfig));
                return gson.fromJson(Files.readString(configFile.toPath()), Config.class);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}