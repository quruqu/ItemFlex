package me.ujun;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Command implements CommandExecutor {

    private final Map<UUID, ItemStack[]> flexedInventory = new HashMap<>();
    private final Map<UUID, ItemStack[]> flexedEnderChest = new HashMap<>();
    private final Map<UUID, ItemStack> flexedHandItem = new HashMap<>();
    private final Map<UUID, UUID> viewingPlayers;
    private final Main plugin;
    Map<String, String> placeholders = new HashMap<>();

    public Command(Map<UUID, UUID> viewingPlayers, Main plugin) {
        this.viewingPlayers = viewingPlayers;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("showitem")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "only player can use this command");
                return false;
            }

            Player player = (Player) sender;
            
            if (args.length != 1) {
                player.sendMessage(plugin.getConfig().getString("showitem.error", "형식 잘못됨"));
                return false;
            }

            String inventory_message = null;
            String playerName = player.getName();
            
            
            String subcommand = args[0];
            switch (subcommand) {
                case "mainhand":
                    ItemStack copyItem = player.getInventory().getItemInMainHand().clone();

                    if (copyItem.isEmpty()) {
                        player.sendMessage(plugin.getConfig().getString("showitem.no_item", "아이템이 없습니다."));
                        return false;
                    }

                    flexedHandItem.put(player.getUniqueId(), copyItem);
                    inventory_message = "mainhand";
                    break;
                case "inventory":
                    ItemStack[] originalInventory = player.getInventory().getContents().clone();

                    if (isInventoryEmpty(originalInventory)) {
                        player.sendMessage(plugin.getConfig().getString("showitem.no_item", "아이템이 없습니다."));
                        return false;
                    }

                    flexedInventory.put(player.getUniqueId(), copyInventory(originalInventory));
                    inventory_message = "player_inventory";
                    break;
                case "enderchest":
                    ItemStack[] originalEnderChest = player.getEnderChest().getContents().clone();

                    if (isInventoryEmpty(originalEnderChest)) {
                        player.sendMessage(plugin.getConfig().getString("showitem.no_item", "아이템이 없습니다."));
                        return false;
                    }

                    flexedEnderChest.put(player.getUniqueId(), copyInventory(originalEnderChest));
                    inventory_message = "enderchest";
                    break;
                default:
                    player.sendMessage(plugin.getConfig().getString("showitem.error", "형식 잘못됨"));
            
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

        if (command.getName().equalsIgnoreCase("seeitem")) {
            if (args.length != 2) {
                sender.sendMessage(plugin.getConfig().getString("seeitem.error", "형식 잘못됨"));
                return true;
            }

            Player viewer = (Player) sender;
            String targetName = args[0];
            String subcommand = args[1];

            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                viewer.sendMessage(plugin.getConfig().getString("seeitem.no_player", " §c플레이어를 첮울 수 없습니다!"));
                return true;
            }

            switch (subcommand) {
                case "inventory":
                    seeInventoryGui(target, viewer);
                    break;
                case "enderchest":
                    seeEnderChestGui(target, viewer);
                    break;
                case "mainhand":
                    seeHandItem(target, viewer);
                    break;
                default:
            }
            return true;

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
        if (!flexedHandItem.containsKey(target.getUniqueId())) {
            viewer.sendMessage(plugin.getConfig().getString("seeitem.no_item", " §c아이템이 없습니다!"));
            return;
        }

        viewingPlayers.put(viewer.getUniqueId(), target.getUniqueId());

        ItemStack handItem = flexedHandItem.get(target.getUniqueId()); // 메인 핸드 아이템


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

    private void seeEnderChestGui(Player target, Player viewer) {
        if (!flexedEnderChest.containsKey(target.getUniqueId())) {
            viewer.sendMessage(plugin.getConfig().getString("seeitem.no_item", " §c아이템이 없습니다!"));
            return;
        }

        viewingPlayers.put(viewer.getUniqueId(), target.getUniqueId());

        ItemStack[] items = flexedEnderChest.get(target.getUniqueId());
        int size = Math.min(items.length, 27); // 9단위로 맞추고 최대 54칸

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


    private void seeInventoryGui(Player target, Player viewer) {

        if (!flexedInventory.containsKey(target.getUniqueId())) {
            viewer.sendMessage(plugin.getConfig().getString("seeitem.no_item", " §c아이템이 없습니다!"));
            return;
        }

        viewingPlayers.put(viewer.getUniqueId(), target.getUniqueId());

        ItemStack[] items = flexedInventory.get(target.getUniqueId());
        ItemStack[] armorItems = new ItemStack[4]; // 갑옷 슬롯 (4개)
        System.arraycopy(items, 36, armorItems, 0, 4); // 36번부터 39번까지 갑옷

        ItemStack offhandItem = items[40]; // 오프핸드 아이템 (40번)

        String inventoryTitle = plugin.getConfig().getString("player_inventory_message", target.getName()).replace("%player%", target.getName());
        inventoryTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('§', inventoryTitle));
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.BLACK + inventoryTitle);

        //0~3
        for (int i = 0; i < 4; i++) {
            if (armorItems[i] != null && armorItems[i].getType() != Material.AIR) {
                gui.setItem(i, armorItems[i]);
            }
        }

        //5
        if (offhandItem != null && offhandItem.getType() != Material.AIR) {
            gui.setItem(4, offhandItem);
        }

        //9~17
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) {
            gui.setItem(i+9, glass);
        }

        // 나머지 인벤토리 아이템을 3번째 행부터 채움
        for (int i = 9; i < 36; i++) {
            if (items[i] != null && items[i].getType() != Material.AIR) {
                gui.setItem(i + 9, items[i]);
            }
        }

        // 핫바는 마지막 행에 표시
        for (int i = 0; i < 9; i++) {
            if (items[i] != null && items[i].getType() != Material.AIR) {
                gui.setItem(i + 45, items[i]); // 핫바 아이템
            }
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
