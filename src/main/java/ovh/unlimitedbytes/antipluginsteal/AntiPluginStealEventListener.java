package ovh.unlimitedbytes.antipluginsteal;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareCommands;
import com.velocitypowered.api.proxy.Player;

import java.util.Locale;

public class AntiPluginStealEventListener implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Player player = (Player) event.getPlayer();
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Server.DECLARE_COMMANDS) {
            // Remove commands that the player does not have permission to use from showing up in the client
            WrapperPlayServerDeclareCommands wrapper = new WrapperPlayServerDeclareCommands(event);

            wrapper.getNodes().removeIf(node -> {
                String nodeName = node.getName().orElse("").toLowerCase(Locale.ROOT);

                if (nodeName.isEmpty()) {
                    return false;
                }

                return !player.hasPermission("antipluginsteal.bypass." + nodeName);
            });
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.TAB_COMPLETE) {
            // Block commands that the player does not have permission to use from being tab-completed
            WrapperPlayClientTabComplete wrapper = new WrapperPlayClientTabComplete(event);

            if (!wrapper.getText().startsWith("/")) {
                return;
            }

            String command = wrapper.getText().substring(1).toLowerCase(Locale.ROOT);
            String rootCommand = command.split(" ")[0];

            if (!player.hasPermission("antipluginsteal.bypass." + rootCommand)) {
                event.setCancelled(true);
            }
        }
    }
}
