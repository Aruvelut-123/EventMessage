package com.baymaxawa.eventMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class ConfigManager {
    private final Path dataDirectory;

    private final Logger logger;

    private final Path configFile;

    private Map<String, Object> config;

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.configFile = dataDirectory.resolve("config.yml");
    }

    public boolean loadConfig() {
        if (!Files.exists(this.configFile, new java.nio.file.LinkOption[0]))
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.yml");
                try {
                    if (inputStream != null) {
                        Files.copy(inputStream, this.configFile, new java.nio.file.CopyOption[0]);
                        this.logger.info("Default configuration file created.");
                    } else {
                        this.logger.error("Could not find default config.yml in resources.");
                        boolean bool = false;
                        if (inputStream != null)
                            inputStream.close();
                        return bool;
                    }
                    if (inputStream != null)
                        inputStream.close();
                } catch (Throwable throwable) {
                    if (inputStream != null)
                        try {
                            inputStream.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
            } catch (IOException e) {
                this.logger.error("Failed to create default configuration file: " + e.getMessage());
                return false;
            }
        try {
            Yaml yaml = new Yaml();
            this.config = (Map<String, Object>)yaml.load(Files.newInputStream(this.configFile, new java.nio.file.OpenOption[0]));
            if (this.config == null) {
                this.logger.error("Configuration file is empty or invalid.");
                return false;
            }
            return true;
        } catch (IOException e) {
            this.logger.error("Failed to load configuration file: " + e.getMessage());
            return false;
        }
    }

    private Map<String, Object> getMessagesSection() {
        if (this.config != null && this.config.containsKey("messages"))
            return (Map<String, Object>)this.config.get("messages");
        return null;
    }

    private Map<String, Object> getSettingsSection() {
        if (this.config != null && this.config.containsKey("settings"))
            return (Map<String, Object>)this.config.get("settings");
        return null;
    }

    private Map<String, Object> getJoinSection() {
        Map<String, Object> messages = getMessagesSection();
        if (messages != null && messages.containsKey("join"))
            return (Map<String, Object>)messages.get("join");
        return null;
    }

    private Map<String, Object> getLeaveSection() {
        Map<String, Object> messages = getMessagesSection();
        if (messages != null && messages.containsKey("leave"))
            return (Map<String, Object>)messages.get("leave");
        return null;
    }

    private Map<String, Object> getTransferSection() {
        Map<String, Object> messages = getMessagesSection();
        if (messages != null && messages.containsKey("transfer"))
            return (Map<String, Object>)messages.get("transfer");
        return null;
    }

    private Map<String, Object> getLobbySection() {
        if (this.config != null && this.config.containsKey("lobby"))
            return (Map<String, Object>)this.config.get("lobby");
        return null;
    }

    private Map<String, Object> getPrefixSection() {
        Map<String, Object> messages = getMessagesSection();
        if (messages != null && messages.containsKey("prefix"))
            return (Map<String, Object>)messages.get("prefix");
        return null;
    }

    public boolean isJoinMessageEnabled() {
        Map<String, Object> join = getJoinSection();
        if (join != null && join.containsKey("enabled"))
            return Boolean.TRUE.equals(join.get("enabled"));
        return true;
    }

    public String getJoinMessageFormat() {
        Map<String, Object> join = getJoinSection();
        if (join != null && join.containsKey("format"))
            return (String)join.get("format");
        return "&a%player% 加入了服务器";
    }

    public boolean isLeaveMessageEnabled() {
        Map<String, Object> leave = getLeaveSection();
        if (leave != null && leave.containsKey("enabled"))
            return Boolean.TRUE.equals(leave.get("enabled"));
        return true;
    }

    public String getLeaveMessageFormat() {
        Map<String, Object> leave = getLeaveSection();
        if (leave != null && leave.containsKey("format"))
            return (String)leave.get("format");
        return "&c%player% 退出了服务器";
    }

    public boolean isTransferMessageEnabled() {
        Map<String, Object> transfer = getTransferSection();
        if (transfer != null && transfer.containsKey("enabled"))
            return Boolean.TRUE.equals(transfer.get("enabled"));
        return true;
    }

    public String getTransferMessageFormat() {
        Map<String, Object> transfer = getTransferSection();
        if (transfer != null && transfer.containsKey("format"))
            return (String)transfer.get("format");
        return "&e%player%: %from_server% -> %to_server%";
    }

    public String getLobbyName() {
        Map<String, Object> settings = getSettingsSection();
        if (settings != null && settings.containsKey("lobby")) {
            Object lobby_name = settings.get("lobby");
            if (lobby_name instanceof String)
                try {
                    return lobby_name.toString();
                } catch (NumberFormatException e) {
                    this.logger.warn("Invalid lobby value, using default \"lobby\"");
                }
        }
        return "lobby";
    }

    public boolean isDebugEnabled() {
        Map<String, Object> settings = getSettingsSection();
        if (settings != null && settings.containsKey("debug"))
            return Boolean.TRUE.equals(settings.get("debug"));
        return false;
    }

    public boolean isPrefixEnabled() {
        Map<String, Object> settings = getPrefixSection();
        if (settings != null && settings.containsKey("enabled"))
            return Boolean.TRUE.equals(settings.get("enabled"));
        return true;
    }

    public String getPrefixFormat() {
        Map<String, Object> settings = getPrefixSection();
        if (settings != null && settings.containsKey("format"))
            return (String)settings.get("format");
        return "&7[EventMessage] ";
    }

    public long getMessageDelay() {
        Map<String, Object> settings = getSettingsSection();
        if (settings != null && settings.containsKey("message-delay")) {
            Object delay = settings.get("message-delay");
            if (delay instanceof Number)
                return ((Number)delay).longValue();
            if (delay instanceof String)
                try {
                    return Long.parseLong((String)delay);
                } catch (NumberFormatException e) {
                    this.logger.warn("Invalid message-delay value, using default 1000ms");
                }
        }
        return 1000L;
    }

    public void reloadConfig() {
        if (loadConfig()) {
            this.logger.info("Configuration reloaded successfully!");
        } else {
            this.logger.error("Failed to reload configuration!");
        }
    }
}
