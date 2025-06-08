package me.ujun.commands;

import me.ujun.ItemFlexPlugin;
import me.ujun.utils.Storage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ShowitemCMD implements CommandExecutor {

    private final ItemFlexPlugin plugin;
    Map<String, String> placeholders = new HashMap<>();

    public ShowitemCMD(ItemFlexPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("showitem")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "only player can use this command");
                return false;
            }

            Player player = (Player) sender;
            
            if (args.length != 1) {
                player.sendMessage(plugin.getConfig().getString("showitem.error", "§cFormat is incorrect"));
                return false;
            }

            String inventory_message = null;
            String playerName = player.getName();
            
            
            String subcommand = args[0];
            switch (subcommand) {
                case "mainhand":
                    ItemStack copyItem = player.getInventory().getItemInMainHand().clone();

                    if (copyItem.isEmpty()) {
                        player.sendMessage(plugin.getConfig().getString("showitem.no_item", "§cno item"));
                        return false;
                    }

                    Storage.flexedHandItem.put(player.getUniqueId(), copyItem);
                    inventory_message = "mainhand";
                    break;
                case "inventory":
                    ItemStack[] originalInventory = player.getInventory().getContents().clone();

                    if (isInventoryEmpty(originalInventory)) {
                        player.sendMessage(plugin.getConfig().getString("showitem.no_item", "§cno item"));
                        return false;
                    }

                    Storage.flexedInventory.put(player.getUniqueId(), copyInventory(originalInventory));
                    inventory_message = "player_inventory";
                    break;
                case "enderchest":
                    ItemStack[] originalEnderChest = player.getEnderChest().getContents().clone();

                    if (isInventoryEmpty(originalEnderChest)) {
                        player.sendMessage(plugin.getConfig().getString("showitem.no_item", "§cno item"));
                        return false;
                    }

                    Storage.flexedEnderChest.put(player.getUniqueId(), copyInventory(originalEnderChest));
                    inventory_message = "enderchest";
                    break;
                default:
                    player.sendMessage(plugin.getConfig().getString("showitem.error", "§cFormat is incorrect"));
            
            }

            placeholders.put("player", playerName);
            placeholders.put("inventory_message", plugin.getConfig().getString( inventory_message + "_message"));


            if (inventory_message != null) {
                TextComponent text = new TextComponent(getFormattedMessage("showitem_message", placeholders));
                text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/seeitem " + playerName + " " + subcommand));

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.spigot().sendMessage(text);
                }
            }
        }


        return false;
    }


    private ItemStack[] copyInventory(ItemStack[] original) {
        ItemStack[] clone = new ItemStack[original.length];

        for (int i = 0; i < original.length; i++) {
            if (original[i] != null) {
                clone[i] = original[i].clone();
            }
        }
        return clone;
    }


     private boolean isInventoryEmpty(ItemStack[] contents) {
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }



    private void seeHandItem(Player target, Player viewer) {
        if (!Storage.flexedHandItem.containsKey(target.getUniqueId())) {
            viewer.sendMessage(plugin.getConfig().getString("seeitem.no_item", "§cno item"));
            return;
        }

        Storage.viewingPlayers.put(viewer.getUniqueId(), target.getUniqueId());

        ItemStack handItem = Storage.flexedHandItem.get(target.getUniqueId()); // 메인 핸드 아이템


        String inventoryTitle = plugin.getConfig().getString("mainhand_message", target.getName()).replace("%player%", target.getName());
        inventoryTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('§', inventoryTitle));
        Inventory gui = Bukkit.createInventory(null, 9,  ChatColor.BLACK + inventoryTitle);

        if (handItem != null && handItem.getType() != Material.AIR) {
            gui.setItem(4, handItem);
        } else {
            gui.setItem(4, new ItemStack(Material.AIR));
        }

        viewer.openInventory(gui);
    }


    public String getFormattedMessage(String key, Map<String, String> placeholders) {
        String message = plugin.getConfig().getString(key);


        Pattern pattern = Pattern.compile("%([^%]+)%");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String placeholderKey = matcher.group(1);
            String replaceValue = plugin.getConfig().getString(placeholderKey);

            if (replaceValue != null) {
                message = message.replace("%" + placeholderKey + "%", replaceValue);
            }
        }

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return message;
    }

}
