package lib.github.joelgodofwar.coreutils.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class JsonMessageUtils2 {

    private static final boolean IS_FOLIA;
    private static final Method GET_SCHEDULER_METHOD;
    private static final Method RUN_METHOD;
    ColorCodeFixer colorCodeFixer = CoreUtils.ColorCodeFixer;

    static {
        boolean isFolia;
        Method getSchedulerMethod = null;
        Method runMethod = null;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
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
            isFolia = false;
        }
        IS_FOLIA = isFolia;
        GET_SCHEDULER_METHOD = getSchedulerMethod;
        RUN_METHOD = runMethod;
    }

    private final JavaPlugin plugin;

    public JsonMessageUtils2(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a message to a player, using MiniMessage for Paper/Folia if applicable, or JSON for Spigot.
     * Assumes any placeholders (e.g., <player>) have already been replaced in the calling code.
     * @param player The player to send the message to.
     * @param message The input message (raw, MiniMessage, or JSON).
     */
    public void sendMessage(Player player, String message) {
        String serverType = plugin.getServer().getName();
        if (serverType.contains("Paper") || serverType.contains("Folia")) {
            String formatted = colorCodeFixer.fixColorsPaper(message);
            Component component = tryMiniMessageDeserialize(formatted);
            if (component != null) {
                sendAdventureMessage(player, component);
                return;
            }
            // Fallback: Treat as JSON
            try {
                component = GsonComponentSerializer.gson().deserialize(formatted);
                sendAdventureMessage(player, component);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse message as MiniMessage or JSON for " + player.getName() + ": " + e.getMessage());
                // Ultimate fallback: Plain text
                sendAdventureMessage(player, Component.text(formatted));
            }
        } else {
            String formatted = colorCodeFixer.fixColorsSpigot(message);
            try {
                sendSpigotMessage(player, new TextComponent(formatted));
            } catch (Exception fallbackE) {
                player.sendMessage(formatted); // Last resort
            }
        }
    }

    /**
     * Sends a pre-processed JSON string to a player.
     * On Paper/Folia: Parses to Adventure Component and sends natively.
     * On Spigot: Parses to TextComponent and sends via Spigot API.
     * @param player The player to send the message to.
     * @param json The JSON string representing the message.
     */
    public void sendJsonMessage(Player player, String json) {
        try {
            String serverType = plugin.getServer().getName();
            if (serverType.contains("Paper") || serverType.contains("Folia")) {
                // On Paper/Folia, try MiniMessage first (in case it's a tag string passed as "JSON")
                Component component = tryMiniMessageDeserialize(json);
                if (component == null) {
                    // Fallback to JSON deserialization
                    component = GsonComponentSerializer.gson().deserialize(json);
                }
                sendAdventureMessage(player, component);
            } else {
                // Fallback to Spigot TextComponent
                TextComponent component = jsonToTextComponent(json);
                sendSpigotMessage(player, component);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send JSON message to " + player.getName() + ": " + e.getMessage());
            // Ultimate fallback: Plain text via Spigot
            try {
                sendSpigotMessage(player, new TextComponent(json));
            } catch (Exception fallbackE) {
                player.sendMessage(json); // Last resort
            }
        }
    }

    /**
     * Attempts to deserialize the input as MiniMessage. Returns null if it fails.
     * Handles both tagged strings and plain text safely.
     */
    private Component tryMiniMessageDeserialize(String input) {
        try {
            return MiniMessage.miniMessage().deserialize(input);
        } catch (Exception e) {
            // Not valid MiniMessage; return null for fallback
            return null;
        }
    }

    /**
     * Internal: Sends an Adventure Component, with Folia scheduling if needed.
     */
    private void sendAdventureMessage(Player player, Component component) {
        if (IS_FOLIA && GET_SCHEDULER_METHOD != null && RUN_METHOD != null) {
            try {
                Object scheduler = GET_SCHEDULER_METHOD.invoke(player);
                Consumer<Object> task = (scheduledTask) -> player.sendMessage(component);
                RUN_METHOD.invoke(scheduler, plugin, task, null);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to schedule Folia message for " + player.getName() + ": " + e.getMessage());
                // Fallback to direct send
                player.sendMessage(component);
            }
        } else {
            player.sendMessage(component);
        }
    }

    /**
     * Internal: Sends a Spigot TextComponent.
     */
    private void sendSpigotMessage(Player player, TextComponent component) {
        if (IS_FOLIA && GET_SCHEDULER_METHOD != null && RUN_METHOD != null) {
            try {
                Object scheduler = GET_SCHEDULER_METHOD.invoke(player);
                Consumer<Object> task = (scheduledTask) -> player.spigot().sendMessage(component);
                RUN_METHOD.invoke(scheduler, plugin, task, null);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to schedule Folia Spigot message for " + player.getName() + ": " + e.getMessage());
                // Fallback to direct send
                player.spigot().sendMessage(component);
            }
        } else {
            player.spigot().sendMessage(component);
        }
    }

    public Component componentFromJson(String json) {
        return GsonComponentSerializer.gson().deserialize(json);
    }

    public TextComponent jsonToTextComponent(String json) {
        TextComponent result = new TextComponent();
        JsonElement jsonElement = JsonParser.parseString(json);

        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                result.addExtra(parseJsonElement(element));
            }
        } else {
            result.addExtra(parseJsonElement(jsonElement));
        }

        return result;
    }

    private TextComponent parseJsonElement(JsonElement element) {
        TextComponent component = new TextComponent();

        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("text")) {
                component.setText(jsonObject.get("text").getAsString());
            }
            if (jsonObject.has("color")) {
                String color = jsonObject.get("color").getAsString();
                component.setColor(ChatColor.of(color.toUpperCase()));
            }
            if (jsonObject.has("bold") && jsonObject.get("bold").getAsBoolean()) {
                component.setBold(true);
            }
            if (jsonObject.has("italic") && jsonObject.get("italic").getAsBoolean()) {
                component.setItalic(true);
            }
            if (jsonObject.has("underlined") && jsonObject.get("underlined").getAsBoolean()) {
                component.setUnderlined(true);
            }
            if (jsonObject.has("strikethrough") && jsonObject.get("strikethrough").getAsBoolean()) {
                component.setStrikethrough(true);
            }
            if (jsonObject.has("obfuscated") && jsonObject.get("obfuscated").getAsBoolean()) {
                component.setObfuscated(true);
            }
            if (jsonObject.has("clickEvent")) {
                JsonObject clickEvent = jsonObject.get("clickEvent").getAsJsonObject();
                String action = clickEvent.get("action").getAsString();
                String value = clickEvent.get("value").getAsString();
                component.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(action.toUpperCase()), value));
            }
            if (jsonObject.has("hoverEvent")) {
                JsonObject hoverEvent = jsonObject.get("hoverEvent").getAsJsonObject();
                String action = hoverEvent.get("action").getAsString();
                String contents = hoverEvent.has("contents") ? hoverEvent.get("contents").getAsString() : "";
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(action.toUpperCase()), new Text(contents)));
            }
            if (jsonObject.has("extra")) {
                JsonArray extra = jsonObject.get("extra").getAsJsonArray();
                for (JsonElement extraElement : extra) {
                    component.addExtra(parseJsonElement(extraElement));
                }
            }
        } else if (element.isJsonPrimitive()) {
            component.setText(element.getAsString());
        }

        return component;
    }
}