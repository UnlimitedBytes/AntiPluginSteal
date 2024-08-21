package ovh.unlimitedbytes.antipluginsteal;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Locale;

@Plugin(
    id = "antipluginsteal",
    name = "AntiPluginSteal",
    version = BuildConstants.VERSION
)
public class AntiPluginSteal {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public AntiPluginSteal(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        PluginContainer pluginContainer = server.getPluginManager().ensurePluginContainer(this);
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, pluginContainer, logger, dataDirectory));
        PacketEvents.getAPI().getSettings().debug(false).checkForUpdates(false);
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().init();

        logger.info("AntiPluginSteal has been initialized!");
    }

    @Subscribe(order = PostOrder.EARLY)
    @SuppressWarnings("UnstableApiUsage")
    public void onCommandInfo(PlayerAvailableCommandsEvent event) {
        // Block commands that the player does not have permission to use from showing up in brigadier
        var player = event.getPlayer();
        var rootNode = event.getRootNode();

        rootNode.getChildren().removeIf(command -> {
            String commandName = command.getName().toLowerCase(Locale.ROOT);

            return !player.hasPermission("antipluginsteal.bypass." + commandName);
        });
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onTabCompleteEvent(TabCompleteEvent event) {
        // Block tab completions for commands that the player does not have permission to use
        String rootCommand = event.getPartialMessage().split(" ")[0].toLowerCase(Locale.ROOT);

        if (
            event.getPlayer().hasPermission("antipluginsteal.bypass." + rootCommand)
        ) {
            return;
        }

        event.getSuggestions().clear();
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onCommandExecution(CommandExecuteEvent event) {
        // Block execution of commands that the player does not have permission to use
        String rootCommand = event.getCommand().split(" ")[0].toLowerCase(Locale.ROOT);

        if (
            event.getCommandSource().hasPermission("antipluginsteal.bypass." + rootCommand)
        ) {
            return;
        }

        event.setResult(CommandExecuteEvent.CommandResult.denied());
        event.getCommandSource().sendMessage(MiniMessage.miniMessage().deserialize(
            "<red><lang:command.unknown.command><newline><click:suggest_command:/" + event.getCommand() + "><u>" +
            event.getCommand() + "</u><i><lang:command.context.here></i></click>"
        ));
    }


}
