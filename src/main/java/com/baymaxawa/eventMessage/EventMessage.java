package com.baymaxawa.eventMessage;

import com.google.inject.Inject;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.*;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.stream.Stream;
import org.slf4j.Logger;

@Plugin(id = "event-message", name = "EventMessage", description = "A Velocity Plugin that can output player join messages through trchat for broadcast.", version = "1.0.0-SNAPSHOT", authors = {"Baymaxawa"})
public final class EventMessage {
    private final ProxyServer server;

    private final Logger logger;

    private final Path dataDirectory;

    private final CommandManager commandManager;

    public ConfigManager configManager;

    public static EventMessage instance;

    @Inject
    public EventMessage(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.commandManager = server.getCommandManager();
        this.dataDirectory = dataDirectory;
        instance = this;
        logger.info("EventMessage plugin is loading...");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        EventMessageCommand eventMessageCommand = new EventMessageCommand();
        BrigadierCommand brigadierCommand = eventMessageCommand.createCommand(this.server);

        CommandMeta eventMessageMeta = this.commandManager.metaBuilder("eventmessage")
                .plugin(this)
                .aliases(new String[] { "em" }).build();
        this.commandManager.register(eventMessageMeta, (Command)brigadierCommand);
        try {
            Files.createDirectories(this.dataDirectory, (FileAttribute<?>[])new FileAttribute[0]);
        } catch (IOException e) {
            this.logger.error("Failed to create plugin directory: {}", e.getMessage());
            return;
        }
        this.configManager = new ConfigManager(this.dataDirectory, this.logger);
        if (this.configManager.loadConfig()) {
            this.logger.info("Configuration loaded successfully!");
        } else {
            this.logger.error("Failed to load configuration!");
        }
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        if (this.configManager == null || !this.configManager.isJoinMessageEnabled())
            return;
        String playerName = event.getPlayer().getUsername();
        String message = this.configManager.getJoinMessageFormat()
                .replace("%player%", playerName);
        if (!event.getPlayer().hasPermission("eventmessage.bypass"))
            event.getPlayer().sendRichMessage(translateColorCodes(message));
        this.server.getAllServers().stream()
                .flatMap(serverInfo -> serverInfo.getPlayersConnected().stream())
                //.filter(player -> !player.hasPermission("eventmessage.bypass"))
                .forEach(player -> player.sendRichMessage(translateColorCodes(message)));
        if (this.configManager.isDebugEnabled())
            this.logger.info("Join message sent for player: {}", playerName);
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        if (this.configManager == null || !this.configManager.isLeaveMessageEnabled())
            return;
        String playerName = event.getPlayer().getUsername();
        String message = this.configManager.getLeaveMessageFormat()
                .replace("%player%", playerName);
        if (!event.getPlayer().hasPermission("eventmessage.bypass"))
            event.getPlayer().sendRichMessage(translateColorCodes(message));
        this.server.getAllServers().stream()
                .flatMap(serverInfo -> serverInfo.getPlayersConnected().stream())
                //.filter(player -> !player.hasPermission("eventmessage.bypass"))
                .forEach(player -> player.sendRichMessage(translateColorCodes(message)));
        if (this.configManager.isDebugEnabled())
            this.logger.info("Leave message sent for player: {}", playerName);
    }

    @Subscribe
    public void onPlayerTransfer(ServerConnectedEvent event) {
        if (this.configManager == null || !this.configManager.isTransferMessageEnabled())
            return;
        String playerName = event.getPlayer().getUsername();
        String fromServer = event.getPreviousServer()
                .map(server -> server.getServerInfo().getName())
                .orElse("unknown");
        String toServer = event.getServer().getServerInfo().getName();
        if (toServer.equals(this.configManager.getLobbyName()))
            return;
        String message = this.configManager.getTransferMessageFormat()
                .replace("%player%", playerName)
                .replace("%from_server%", fromServer)
                .replace("%to_server%", toServer);
        if (!event.getPlayer().hasPermission("eventmessage.bypass"))
            event.getPlayer().sendRichMessage(translateColorCodes(message));
        this.server.getAllServers().stream()
                .flatMap(serverInfo -> serverInfo.getPlayersConnected().stream())
                //.filter(player -> !player.hasPermission("eventmessage.bypass"))
                .forEach(player -> player.sendRichMessage(translateColorCodes(message)));
        if (this.configManager.isDebugEnabled())
            this.logger.info("Transfer message sent for player: {} from {} to {}", playerName, fromServer, toServer);
    }

    private String translateColorCodes(String message) {
        if (this.configManager.isPrefixEnabled())
            message = this.configManager.getPrefixFormat() + this.configManager.getPrefixFormat();
        return message;
    }
}
