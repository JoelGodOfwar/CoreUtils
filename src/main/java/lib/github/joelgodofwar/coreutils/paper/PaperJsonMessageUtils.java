package lib.github.joelgodofwar.coreutils.paper;

import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.JsonMessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        String fixedColors = coreUtils.colorCodeFixer.fixColorsPaper(message);
        if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendMessage ENTRY for " + player.getName() + " | Input len: " + fixedColors.length() + " | Starts: " + fixedColors.substring(0, Math.min(50, fixedColors.length())) + "...");

        Component component = tryMiniMessageDeserialize(fixedColors);
        if (component == null) {
            try {
                if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendMessage: Trying Gson deserial");
                component = GsonComponentSerializer.gson().deserialize(fixedColors);
                if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendMessage: Gson SUCCEEDED - sending Component");
            } catch (Exception e) {
                if(CoreUtils.debug) plugin.getLogger().severe("[CoreUtils Paper] sendMessage Gson FAILED for " + player.getName() + ": " + e.getMessage() + " | Full input: " + fixedColors);  // Severe for visibility
                component = Component.text(fixedColors);
                if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendMessage: Fell back to raw text");
            }
        } else {
            if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendMessage: MiniMessage SUCCEEDED");
        }
        sendAdventureMessage(player, component);
        if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendMessage EXIT: Sent to " + player.getName());
    }

    @Override
    public void sendJsonMessage(Player player, String message) {
        if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendJsonMessage ENTRY for " + player.getName() + " | Input len: " + message.length() + " | Starts: " + message.substring(0, Math.min(50, message.length())) + "...");
        String fixedColors = coreUtils.colorCodeFixer.fixColorsPaper(message);
        String inputForCheck = fixedColors.trim();  // Trim for clean comparison

        try {
            // Step 1: Try MiniMessage first (handles tags if present)
            if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendJsonMessage: Trying MiniMessage");
            Component component = MiniMessage.miniMessage().deserialize(inputForCheck);

            // Step 2: Check if MiniMessage truly applied formatting (plain output == input?)
            String plainOutput = LegacyComponentSerializer.legacySection().serialize(component);
            if (plainOutput.equals(inputForCheck)) {
                // No formatting (e.g., JSON literal) → Fallback to Gson
                if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendJsonMessage: MiniMessage was literal (no tags) → Fallback to Gson");
                component = GsonComponentSerializer.gson().deserialize(fixedColors);
                if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendJsonMessage: Gson SUCCEEDED - rich JSON parsed");
            } else {
                if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendJsonMessage: MiniMessage applied formatting → Using it");
            }
            sendAdventureMessage(player, component);
        } catch (Exception e) {
            if(CoreUtils.debug) plugin.getLogger().severe("[CoreUtils Paper] sendJsonMessage FAILED for " + player.getName() + ": " + e.getMessage() + " | Input snippet: " + message.substring(0, 300));
            sendAdventureMessage(player, Component.text(fixedColors));
            if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendJsonMessage: Fell back to raw text");
        }
        if(CoreUtils.debug) plugin.getLogger().info("[CoreUtils Paper] sendJsonMessage EXIT: Sent to " + player.getName());
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
