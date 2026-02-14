package com.baymaxawa.eventMessage;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class EventMessageCommand {
    public BrigadierCommand createCommand(ProxyServer proxy) {
        LiteralCommandNode<CommandSource> literalCommandNode = LiteralArgumentBuilder.<CommandSource>literal("eventmessage")
                .requires(source -> source.hasPermission("eventmessage.use"))
                .executes(this::executeMain)
                .then(
                        LiteralArgumentBuilder.<CommandSource>literal("help")
                                .executes(this::executeHelp))
                .then(
                        LiteralArgumentBuilder.<CommandSource>literal("reload")
                                .requires(source -> source.hasPermission("eventmessage.reload"))
                                .executes(this::executeReload))
                .build();
        return new BrigadierCommand(literalCommandNode);
    }

    private int executeMain(CommandContext<CommandSource> context) {
        CommandSource source = (CommandSource)context.getSource();
        source.sendMessage((Component)Component.text("=== EventMessage 帮助 ===", (TextColor)NamedTextColor.GOLD));
        source.sendMessage((Component)Component.text("/eventmessage help - 显示帮助", (TextColor)NamedTextColor.YELLOW));
        source.sendMessage((Component)Component.text("/eventmessage reload - 重载配置", (TextColor)NamedTextColor.YELLOW));
        return 1;
    }

    private int executeHelp(CommandContext<CommandSource> context) {
        CommandSource source = (CommandSource)context.getSource();
        source.sendMessage((Component)Component.text("=== EventMessage 帮助 ===", (TextColor)NamedTextColor.GOLD));
        source.sendMessage((Component)Component.text("/eventmessage help - 显示帮助", (TextColor)NamedTextColor.YELLOW));
        source.sendMessage((Component)Component.text("/eventmessage reload - 重载配置", (TextColor)NamedTextColor.YELLOW));
        return 1;
    }

    private int executeReload(CommandContext<CommandSource> context) {
        CommandSource source = (CommandSource)context.getSource();
        EventMessage.instance.configManager.reloadConfig();
        source.sendMessage((Component)Component.text("配置已重载！", (TextColor)NamedTextColor.GREEN));
        return 1;
    }
}
