package lib.github.joelgodofwar.coreutils.folia;

import com.earth2me.essentials.Essentials;
import dev.majek.hexnicks.HexNicks;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.ServerHandler;
import lib.github.joelgodofwar.coreutils.util.common.PluginLibrary;
import lib.github.joelgodofwar.coreutils.util.error.ErrorReporter;
import lib.github.joelgodofwar.coreutils.util.error.Report;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FoliaHandler implements ServerHandler {
    private static final Method GET_SCHEDULER_METHOD;
    private static final Method RUN_METHOD;

    static {
        Method getSchedulerMethod = null;
        Method runMethod = null;
        try {
            Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            getSchedulerMethod = Player.class.getMethod("getScheduler");
            for (Method method : entitySchedulerClass.getMethods()) {
                if (method.getName().equals("run") && method.getParameterCount() == 3) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params[0].equals(Object.class) && params[1].equals(Consumer.class) && params[2].equals(Runnable.class)) {
                        runMethod = method;
                        break;
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // Folia-specific classes/methods not found
        }
        GET_SCHEDULER_METHOD = getSchedulerMethod;
        RUN_METHOD = runMethod;
    }

    @Override
    public String fixColors(String message) {
        return CoreUtils.ColorCodeFixer.fixColorsPaper(message);
    }

    @Override
    public void broadcast(CoreUtils coreUtils, Plugin plugin, String jsonMessage) {
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                coreUtils.jsonMessageUtils.sendJsonMessage(player, jsonMessage);
            }
        });
    }

    @Override
    public void sendJsonMessage(CoreUtils coreUtils, Plugin plugin, Player player, String jsonMessage) {
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            coreUtils.jsonMessageUtils.sendJsonMessage(player, jsonMessage);
        });
    }

    @Override
    public void getNicknameAsync(CoreUtils coreUtils, Plugin plugin, Player player, boolean useDisplayName, Consumer<String> callback) {
        // NEW: No outer global wrap
        String playerName = player.getName();
        ErrorReporter reporter = PluginLibrary.getErrorReporter();
        try {
            PluginManager pluginManager = player.getServer().getPluginManager();
            if (CoreUtils.debug) {
                plugin.getLogger().fine("Folia: player.getDisplayName()=" + player.getDisplayName());
                plugin.getLogger().fine("Folia: player.getName()=" + player.getName());
                plugin.getLogger().fine("Folia: useDisplayName=" + useDisplayName);
            }

            playerName = useDisplayName ? coreUtils.colorCodeFixer.fixColorsPaper(player.getDisplayName()) : player.getName();

            if (pluginManager.getPlugin("VentureChat") != null) {
                MineverseChatPlayer mcp = MineverseChatAPI.getMineverseChatPlayer(player);
                String nick = mcp.getNickname();
                if (nick != null) {
                    if (CoreUtils.debug) plugin.getLogger().fine("Folia: VentureChat Nick=" + nick);
                    nick = coreUtils.colorCodeFixer.fixColorsPaper(nick);
                    if (CoreUtils.debug) plugin.getLogger().fine("Folia: VentureChat Formatted Nick=" + nick);
                    callback.accept(nick);
                    return;
                }
                if (CoreUtils.debug) plugin.getLogger().fine("Folia: VentureChat Nick=null using " + playerName);
                callback.accept(coreUtils.colorCodeFixer.fixColorsPaper(playerName));
                return;
            }
            if (pluginManager.getPlugin("Essentials") != null) {
                Essentials ess = (Essentials) pluginManager.getPlugin("Essentials");  // CHANGED: Removed assert
                String nick = ess.getUserMap().getUser(player.getName()).getNickname();
                if (nick != null) {
                    if (CoreUtils.debug) plugin.getLogger().fine("Folia: Essentials Nick=" + nick);
                    callback.accept(coreUtils.colorCodeFixer.fixColorsPaper(nick));
                    return;
                }
                if (CoreUtils.debug) plugin.getLogger().fine("Folia: Essentials Nick=null using " + playerName);
                callback.accept(coreUtils.colorCodeFixer.fixColorsPaper(playerName));
                return;
            }
            if (pluginManager.getPlugin("HexNicks") != null) {
                CompletableFuture<Component> nickFuture = HexNicks.api().getStoredNick(player);
                String finalPlayerName = playerName;
                nickFuture.thenAccept(nickComponent -> {
                    String nick = nickComponent != null ? GsonComponentSerializer.gson().serialize(nickComponent) : finalPlayerName;
                    player.getScheduler().run(plugin, (task) -> {  // FIXED: Immediate run, 1-param lambda
                        if (CoreUtils.debug) plugin.getLogger().fine("Folia: HexNicks Serialized Nick=" + nick);
                        callback.accept(nick);
                    }, null);
                }).exceptionally(e -> {
                    if (CoreUtils.debug) plugin.getLogger().warning("Folia: Error getting HexNicks nickname: " + e.getMessage());
                    reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_GET_HEXNICK).error((Exception) e).build());
                    player.getScheduler().run(plugin, (task) -> callback.accept(coreUtils.colorCodeFixer.fixColorsPaper(finalPlayerName)), null);  // FIXED: Same
                    return null;
                });
                return;
            }
            if (CoreUtils.debug) plugin.getLogger().fine("Folia: No nickname found, using=" + playerName);
            callback.accept(coreUtils.colorCodeFixer.fixColorsPaper(playerName));
        } catch (Exception e) {
            plugin.getLogger().warning("Folia: Error getting nickname: " + e.getMessage());
            reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_ERROR_GETTING_NICKNAME).error(e).build());
            callback.accept(coreUtils.colorCodeFixer.fixColorsPaper(playerName));
        }
    }
}