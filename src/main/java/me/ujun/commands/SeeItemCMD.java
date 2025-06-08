package me.ujun.commands;

import me.ujun.ItemFlexPlugin;
import me.ujun.utils.Storage;
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

public class SeeItemCMD implements CommandExecutor {

    private final ItemFlexPlugin plugin;

    public SeeItemCMD(ItemFlexPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label,@NotNull String[] args) {
        if (!(sender instanceof Player viewer)) return true;

        if (args.length != 2) {
            viewer.sendMessage(plugin.getConfig().getString("seeitem.error", "§cFormat is incorrect"));
            return true;
        }

        String targetName = args[0];
        String subcommand = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            viewer.sendMessage(plugin.getConfig().getString("seeitem.no_player", "§cno player"));
            return true;
        }

        switch (subcommand) {
            case "inventory" -> seeInventoryGui(target, viewer);
            case "enderchest" -> seeEnderChestGui(target, viewer);
            case "mainhand" -> seeHandItem(target, viewer);
        }

        return true;
    }

    private void seeInventoryGui(Player target, Player viewer) {

        if (!Storage.flexedInventory.containsKey(target.getUniqueId())) {
            viewer.sendMessage(plugin.getConfig().getString("seeitem.no_item", "§cno item"));
            return;
        }

        Storage.viewingPlayers.put(viewer.getUniqueId(), target.getUniqueId());

        ItemStack[] items = Storage.flexedInventory.get(target.getUniqueId());
        ItemStack[] armorItems = new ItemStack[4]; // 갑옷 슬롯 (4개)
        System.arraycopy(items, 36, armorItems, 0, 4); // 36번부터 39번까지 갑옷

        ItemStack offhandItem = items[40]; // 오프핸드 아이템 (40번)

        String inventoryTitle = plugin.getConfig().getString("player_inventory_message", target.getName()).replace("%player%", target.getName());
        inventoryTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('§', inventoryTitle));
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.BLACK + inventoryTitle);

        //armor inventory
        for (int i = 0; i < 4; i++) {
            if (armorItems[i] != null && armorItems[i].getType() != Material.AIR) {
                gui.setItem(i, armorItems[i]);
            }
        }

        //offhand inventory
        if (offhandItem != null && offhandItem.getType() != Material.AIR) {
            gui.setItem(4, offhandItem);
        }

        //splitting line
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) {
            gui.setItem(i+9, glass);
        }

        //Inventory excluding hotbar
        for (int i = 9; i < 36; i++) {
            if (items[i] != null && items[i].getType() != Material.AIR) {
                gui.setItem(i + 9, items[i]);
            }
        }

        //hotbar inventory
        for (int i = 0; i < 9; i++) {
            if (items[i] != null && items[i].getType() != Material.AIR) {
                gui.setItem(i + 45, items[i]); // 핫바 아이템
            }
        }


        viewer.openInventory(gui);
    }

    private void seeEnderChestGui(Player target, Player viewer) {
        if (!Storage.flexedEnderChest.containsKey(target.getUniqueId())) {
            viewer.sendMessage(plugin.getConfig().getString("seeitem.no_item", "§cno item"));
            return;
        }

        Storage.viewingPlayers.put(viewer.getUniqueId(), target.getUniqueId());

        ItemStack[] items = Storage.flexedEnderChest.get(target.getUniqueId());
        int size = Math.min(items.length, 27);

        String inventoryTitle = plugin.getConfig().getString("enderchest_message", target.getName()).replace("%player%", target.getName());
        inventoryTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('§', inventoryTitle));
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.BLACK + inventoryTitle);

        for (int i = 0; i < items.length && i < size; i++) {
            if (items[i] != null && items[i].getType() != Material.AIR) {
                gui.setItem(i, items[i]);
            } else {
                gui.setItem(i, new ItemStack(Material.AIR));
            }
        }

        viewer.openInventory(gui);
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


}
