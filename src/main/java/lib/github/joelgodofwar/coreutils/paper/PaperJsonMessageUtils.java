package lib.github.joelgodofwar.coreutils.paper;

import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.JsonMessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperJsonMessageUtils implements JsonMessageUtils {
    private final JavaPlugin plugin;
    private final CoreUtils coreUtils;

    public PaperJsonMessageUtils(JavaPlugin plugin, CoreUtils coreUtils) {
        this.plugin = plugin;
        this.coreUtils = coreUtils;
    }

    @Override
    public void sendMessage(Player player, String message) {
        String formatted = coreUtils.colorCodeFixer.fixColorsPaper(message);
        Component component = tryMiniMessageDeserialize(formatted);
        if (component == null) {
            try {
                component = GsonComponentSerializer.gson().deserialize(formatted);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse for " + player.getName() + ": " + e.getMessage());
                component = Component.text(formatted);
            }
        }
        sendAdventureMessage(player, component);
    }

    @Override
    public void sendJsonMessage(Player player, String json) {
        try {
            Component component = tryMiniMessageDeserialize(json);
            if (component == null) {
                component = GsonComponentSerializer.gson().deserialize(json);
            }
            sendAdventureMessage(player, component);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed JSON send to " + player.getName() + ": " + e.getMessage());
            sendAdventureMessage(player, Component.text(json));
        }
    }

    @Override
    public Object componentFromJson(String json) {
        return GsonComponentSerializer.gson().deserialize(json);
    }

    private Component tryMiniMessageDeserialize(String input) {
        try {
            return MiniMessage.miniMessage().deserialize(input);
        } catch (Exception e) {
            return null;
        }
    }

    private void sendAdventureMessage(Player player, Component component) {
        player.sendMessage(component);
    }
}
