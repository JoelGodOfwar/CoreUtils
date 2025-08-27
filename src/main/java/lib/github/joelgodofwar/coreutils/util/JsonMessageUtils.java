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

public class JsonMessageUtils {

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

    public JsonMessageUtils(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a message to a player, using MiniMessage for Paper/Folia if applicable, or JSON for Spigot.
     * @param player The player to send the message to.
     * @param message The input message (raw, MiniMessage, or JSON).
     * @param nickName The player's nickname for <player> placeholder.
     */
    public void sendMessage(Player player, String message, String nickName) {
        String serverType = plugin.getServer().getName();
        if (serverType.contains("Paper") || serverType.contains("Folia")) {
            String formatted = colorCodeFixer.fixColorsPaper(message);
            if (formatted.matches(".*(<#[0-9A-Fa-f]{6}>|<[a-z_]+>|</[a-z_]+>|\\[<[a-z_]+>|\\[<#[0-9A-Fa-f]{6}>).*")) {
                Component component = MiniMessage.miniMessage().deserialize(formatted.replace("<player>", nickName));
                player.sendMessage(component);
            } else {
                String json = new JsonConverter(plugin).convert(formatted, nickName);
                sendJsonMessage(player, json);
            }
        } else {
            String formatted = colorCodeFixer.fixColorsSpigot(message);
            String json = new JsonConverter(plugin).convert(formatted, nickName);
            sendJsonMessage(player, json);
        }
    }

    /**
     * Sends a pre-processed JSON string as a TextComponent to a player.
     * @param player The player to send the message to.
     * @param json The JSON string representing the message.
     */
    public void sendJsonMessage(Player player, String json) {
        try {
            TextComponent component = jsonToTextComponent(json);
            if (IS_FOLIA && GET_SCHEDULER_METHOD != null && RUN_METHOD != null) {
                Object scheduler = GET_SCHEDULER_METHOD.invoke(player);
                Consumer<Object> task = (scheduledTask) -> player.spigot().sendMessage(component);
                RUN_METHOD.invoke(scheduler, plugin, task, null);
            } else {
                player.spigot().sendMessage(component);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send JSON message to " + player.getName() + ": " + e.getMessage());
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