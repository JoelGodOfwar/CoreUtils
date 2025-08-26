package lib.github.joelgodofwar.coreutils.spigot;

import com.earth2me.essentials.Essentials;
import lib.github.joelgodofwar.coreutils.ServerHandler;
import lib.github.joelgodofwar.coreutils.util.ChatColorUtils;
import lib.github.joelgodofwar.coreutils.util.PluginLibrary;
import lib.github.joelgodofwar.coreutils.util.error.ErrorReporter;
import lib.github.joelgodofwar.coreutils.util.error.Report;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import java.util.function.Consumer;

public class SpigotHandler implements ServerHandler {
    @Override
    public void getNicknameAsync(Plugin plugin, Player player, boolean useDisplayName, Consumer<String> callback) {
        String playerName = player.getName();
        ErrorReporter reporter = PluginLibrary.getErrorReporter();
        try {
            PluginManager pluginManager = player.getServer().getPluginManager();
            plugin.getLogger().fine("Spigot: player.getDisplayName()=" + player.getDisplayName());
            plugin.getLogger().fine("Spigot: player.getName()=" + player.getName());
            plugin.getLogger().fine("Spigot: useDisplayName=" + useDisplayName);

            playerName = useDisplayName ? ChatColorUtils.setColorsByCode(player.getDisplayName()) : player.getName();

            if (pluginManager.getPlugin("VentureChat") != null) {
                MineverseChatPlayer mcp = MineverseChatAPI.getMineverseChatPlayer(player);
                String nick = mcp.getNickname();
                if (nick != null) {
                    plugin.getLogger().fine("Spigot: VentureChat Nick=" + nick);
                    nick = ChatColorUtils.setColorsByCode(nick);
                    plugin.getLogger().fine("Spigot: VentureChat Formatted Nick=" + nick);
                    callback.accept(nick);
                    return;
                }
                plugin.getLogger().fine("Spigot: VentureChat Nick=null using " + playerName);
                callback.accept(ChatColor.translateAlternateColorCodes('&', playerName));
                return;
            }
            if (pluginManager.getPlugin("Essentials") != null) {
                Essentials ess = (Essentials) pluginManager.getPlugin("Essentials");
                String nick = ess.getUserMap().getUser(player.getName()).getNickname();
                if (nick != null) {
                    plugin.getLogger().fine("Spigot: Essentials Nick=" + nick);
                    callback.accept(ChatColor.translateAlternateColorCodes('&', nick));
                    return;
                }
                plugin.getLogger().fine("Spigot: Essentials Nick=null using " + playerName);
                callback.accept(ChatColor.translateAlternateColorCodes('&', playerName));
                return;
            }
            plugin.getLogger().fine("Spigot: No nickname plugins available, using=" + playerName);
            callback.accept(ChatColor.translateAlternateColorCodes('&', playerName));
        } catch (Exception e) {
            plugin.getLogger().warning("Spigot: Error getting nickname: " + e.getMessage());
            reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_ERROR_GETTING_NICKNAME).error(e).build());
            callback.accept(ChatColor.translateAlternateColorCodes('&', playerName));
        }
    }
}