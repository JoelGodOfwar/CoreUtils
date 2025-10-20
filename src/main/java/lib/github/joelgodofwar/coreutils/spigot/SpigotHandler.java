package lib.github.joelgodofwar.coreutils.spigot;

import com.earth2me.essentials.Essentials;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.ServerHandler;
import lib.github.joelgodofwar.coreutils.util.common.PluginLibrary;
import lib.github.joelgodofwar.coreutils.util.error.ErrorReporter;
import lib.github.joelgodofwar.coreutils.util.error.Report;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.function.Consumer;

public class SpigotHandler implements ServerHandler {
    @Override
    public String fixColors(String message) {
        return CoreUtils.ColorCodeFixer.fixColorsSpigot(message);
    }

    @Override
    public void broadcast(CoreUtils coreUtils, Plugin plugin, String jsonMessage) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            coreUtils.jsonMessageUtils.sendJsonMessage(player, jsonMessage);
        }
    }

    @Override
    public void sendJsonMessage(CoreUtils coreUtils, Plugin plugin, Player player, String jsonMessage) {
        coreUtils.jsonMessageUtils.sendJsonMessage(player, jsonMessage);
    }

    @Override
    public void getNicknameAsync(CoreUtils coreUtils, Plugin plugin, Player player, boolean useDisplayName, Consumer<String> callback) {
        String playerName = player.getName();
        ErrorReporter reporter = PluginLibrary.getErrorReporter();
        try {
            PluginManager pluginManager = player.getServer().getPluginManager();
            if (CoreUtils.debug) {
                plugin.getLogger().fine("Spigot: player.getDisplayName()=" + player.getDisplayName());
                plugin.getLogger().fine("Spigot: player.getName()=" + player.getName());
                plugin.getLogger().fine("Spigot: useDisplayName=" + useDisplayName);
            }

            playerName = useDisplayName ? coreUtils.colorCodeFixer.fixColorsSpigot(player.getDisplayName()) : player.getName();

            if (pluginManager.getPlugin("VentureChat") != null) {
                MineverseChatPlayer mcp = MineverseChatAPI.getMineverseChatPlayer(player);
                String nick = mcp.getNickname();
                if (nick != null) {
                    if (CoreUtils.debug) plugin.getLogger().fine("Spigot: VentureChat Nick=" + nick);
                    nick = coreUtils.colorCodeFixer.fixColorsSpigot(nick);
                    if (CoreUtils.debug) plugin.getLogger().fine("Spigot: VentureChat Formatted Nick=" + nick);
                    callback.accept(nick);
                    return;
                }
                if (CoreUtils.debug) plugin.getLogger().fine("Spigot: VentureChat Nick=null using " + playerName);
                callback.accept(coreUtils.colorCodeFixer.fixColorsSpigot(playerName));
                return;
            }
            if (pluginManager.getPlugin("Essentials") != null) {
                Essentials ess = (Essentials) pluginManager.getPlugin("Essentials");
                assert ess != null;
                String nick = ess.getUserMap().getUser(player.getName()).getNickname();
                if (nick != null) {
                    if (CoreUtils.debug) plugin.getLogger().fine("Spigot: Essentials Nick=" + nick);
                    callback.accept(coreUtils.colorCodeFixer.fixColorsSpigot(nick));
                    return;
                }
                if (CoreUtils.debug) plugin.getLogger().fine("Spigot: Essentials Nick=null using " + playerName);
                callback.accept(coreUtils.colorCodeFixer.fixColorsSpigot(playerName));
                return;
            }
            if (CoreUtils.debug) plugin.getLogger().fine("Spigot: No nickname plugins available, using=" + playerName);
            callback.accept(coreUtils.colorCodeFixer.fixColorsSpigot(playerName));
        } catch (Exception e) {
            plugin.getLogger().warning("Spigot: Error getting nickname: " + e.getMessage());
            reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_ERROR_GETTING_NICKNAME).error(e).build());
            callback.accept(coreUtils.colorCodeFixer.fixColorsSpigot(playerName));
        }
    }
}