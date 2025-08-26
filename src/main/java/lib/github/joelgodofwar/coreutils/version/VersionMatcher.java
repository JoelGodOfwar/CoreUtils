package lib.github.joelgodofwar.coreutils.version;


import lib.github.joelgodofwar.coreutils.ServerHandler;
import lib.github.joelgodofwar.coreutils.folia.FoliaHandler;
import lib.github.joelgodofwar.coreutils.paper.PaperHandler;
import lib.github.joelgodofwar.coreutils.spigot.SpigotHandler;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class VersionMatcher {
    public enum ServerType {
        SPIGOT, PAPER, FOLIA, PURPUR, UNKNOWN
    }

    private final ServerType serverType;

    public VersionMatcher(Server server) {
        String version = server.getVersion().toLowerCase();
        String serverName = server.getName().toLowerCase();
        ServerType tempType;

        if (serverName.contains("folia") || version.contains("folia")) {
            // Confirm Folia by checking for RegionScheduler class
            try {
                Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
                tempType = ServerType.FOLIA;
            } catch (ClassNotFoundException e) {
                tempType = ServerType.UNKNOWN;
            }
        } else if (serverName.contains("purpur") || version.contains("purpur")) {
            tempType = ServerType.PURPUR;
        } else if (serverName.contains("paper") || version.contains("paper")) {
            // Confirm Paper by checking for Paper-specific class
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                tempType = ServerType.PAPER;
            } catch (ClassNotFoundException e) {
                tempType = ServerType.UNKNOWN;
            }
        } else if (serverName.contains("spigot") || version.contains("spigot")) {
            tempType = ServerType.SPIGOT;
        } else {
            tempType = ServerType.UNKNOWN;
        }

        this.serverType = tempType;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public String getServerTypeName() {
        return serverType.name();
    }

    public boolean isRunningFolia() {
        return serverType == ServerType.FOLIA;
    }

    public boolean isRunningPaperOrPurpur() {
        return serverType == ServerType.PAPER || serverType == ServerType.PURPUR;
    }

    public boolean isRunningSpigot() {
        return serverType == ServerType.SPIGOT;
    }

    public ServerHandler getServerHandler() {
        if (isRunningFolia()) {
            return new FoliaHandler();
        } else if (isRunningPaperOrPurpur()) {
            return new PaperHandler();
        } else {
            return new SpigotHandler();
        }
    }

    public void runTask(Plugin plugin, Runnable task) {
        if (isRunningFolia()) {
            plugin.getServer().getGlobalRegionScheduler().execute(plugin, task);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }
}