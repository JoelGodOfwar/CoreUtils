package lib.github.joelgodofwar.coreutils.paper;

import com.earth2me.essentials.Essentials;
import dev.majek.hexnicks.HexNicks;
import lib.github.joelgodofwar.coreutils.ServerHandler;
import lib.github.joelgodofwar.coreutils.util.ChatColorUtils;
import lib.github.joelgodofwar.coreutils.util.PluginLibrary;
import lib.github.joelgodofwar.coreutils.util.error.ErrorReporter;
import lib.github.joelgodofwar.coreutils.util.error.Report;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PaperHandler implements ServerHandler {
    @Override
    public void getNicknameAsync(Plugin plugin, Player player, boolean useDisplayName, Consumer<String> callback) {
        String playerName = player.getName();
        ErrorReporter reporter = PluginLibrary.getErrorReporter();
        try {
            PluginManager pluginManager = player.getServer().getPluginManager();
            plugin.getLogger().fine("Paper: player.getDisplayName()=" + player.getDisplayName());
            plugin.getLogger().fine("Paper: player.getName()=" + player.getName());
            plugin.getLogger().fine("Paper: useDisplayName=" + useDisplayName);

            playerName = useDisplayName ? ChatColorUtils.setColorsByCode(player.getDisplayName()) : player.getName();

            if (pluginManager.getPlugin("VentureChat") != null) {
                MineverseChatPlayer mcp = MineverseChatAPI.getMineverseChatPlayer(player);
                String nick = mcp.getNickname();
                if (nick != null) {
                    plugin.getLogger().fine("Paper: VentureChat Nick=" + nick);
                    nick = ChatColorUtils.setColorsByCode(nick);
                    plugin.getLogger().fine("Paper: VentureChat Formatted Nick=" + nick);
                    callback.accept(nick);
                    return;
                }
                plugin.getLogger().fine("Paper: VentureChat Nick=null using " + playerName);
                callback.accept(ChatColor.translateAlternateColorCodes('&', playerName));
                return;
            }
            if (pluginManager.getPlugin("Essentials") != null) {
                Essentials ess = (Essentials) pluginManager.getPlugin("Essentials");
                String nick = ess.getUserMap().getUser(player.getName()).getNickname();
                if (nick != null) {
                    plugin.getLogger().fine("Paper: Essentials Nick=" + nick);
                    callback.accept(ChatColor.translateAlternateColorCodes('&', nick));
                    return;
                }
                plugin.getLogger().fine("Paper: Essentials Nick=null using " + playerName);
                callback.accept(ChatColor.translateAlternateColorCodes('&', playerName));
                return;
            }
            if (pluginManager.getPlugin("HexNicks") != null) {
                CompletableFuture<Component> nickFuture = HexNicks.api().getStoredNick(player);
                String finalPlayerName = playerName;
                nickFuture.thenAccept(nickComponent -> {
                    String nick = GsonComponentSerializer.gson().serialize(nickComponent);
                    plugin.getLogger().fine("Paper: HexNicks Nick=" + nick);
                    if (nick.contains("[")) {
                        nick = nick.substring(nick.indexOf("[") + 1);
                    }
                    if (nick.contains("]")) {
                        nick = nick.substring(0, nick.indexOf("]"));
                    }
                    callback.accept(ChatColor.translateAlternateColorCodes('&', nick));
                }).exceptionally(e -> {
                    plugin.getLogger().warning("Paper: Error getting HexNicks nickname: " + e.getMessage());
                    reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_GET_HEXNICK).error((Exception) e).build());
                    callback.accept(ChatColor.translateAlternateColorCodes('&', finalPlayerName));
                    return null;
                });
                return;
            }
            plugin.getLogger().fine("Paper: No nickname found, using=" + playerName);
            callback.accept(ChatColor.translateAlternateColorCodes('&', playerName));
        } catch (Exception e) {
            plugin.getLogger().warning("Paper: Error getting nickname: " + e.getMessage());
            reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_ERROR_GETTING_NICKNAME).error(e).build());
            callback.accept(ChatColor.translateAlternateColorCodes('&', playerName));
        }
    }
}