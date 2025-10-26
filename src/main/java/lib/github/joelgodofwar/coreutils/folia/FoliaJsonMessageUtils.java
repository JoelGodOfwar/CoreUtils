package lib.github.joelgodofwar.coreutils.folia;

import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.paper.PaperJsonMessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FoliaJsonMessageUtils extends PaperJsonMessageUtils {
    private final JavaPlugin plugin;

    public FoliaJsonMessageUtils(JavaPlugin plugin, CoreUtils coreUtils) {
        super(plugin, coreUtils);
        this.plugin = plugin;  // NEW: Assign it
    }

    @Override
    public void sendMessage(Player player, String message) {
        player.getScheduler().run(this.plugin, (task) -> super.sendMessage(player, message), null);
    }

    @Override
    public void sendJsonMessage(Player player, String message) {
        player.getScheduler().run(this.plugin, (task) -> super.sendJsonMessage(player, message), null);
    }
}
