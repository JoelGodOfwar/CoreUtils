package lib.github.joelgodofwar.coreutils.spigot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.JsonMessageUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotJsonMessageUtils implements JsonMessageUtils {
    private final JavaPlugin plugin;
    private final CoreUtils coreUtils;

    public SpigotJsonMessageUtils(JavaPlugin plugin, CoreUtils coreUtils) {
        this.plugin = plugin;
        this.coreUtils = coreUtils;
    }

    @Override
    public void sendMessage(Player player, String message) {
        String formatted = coreUtils.colorCodeFixer.fixColorsSpigot(message);
        try {
            sendSpigotMessage(player, new TextComponent(formatted));
        } catch (Exception e) {
            player.sendMessage(formatted);
        }
    }

    @Override
    public void sendJsonMessage(Player player, String json) {
        try {
            TextComponent component = jsonToTextComponent(json);
            sendSpigotMessage(player, component);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send JSON to " + player.getName() + ": " + e.getMessage());
            player.sendMessage(json);
        }
    }

    @Override
    public Object componentFromJson(String json) {
        return jsonToTextComponent(json);
    }

    private void sendSpigotMessage(Player player, TextComponent component) {
        player.spigot().sendMessage(component);
    }

    private TextComponent jsonToTextComponent(String json) {
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