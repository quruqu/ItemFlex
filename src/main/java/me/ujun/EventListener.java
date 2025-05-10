package me.ujun;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


public class EventListener implements org.bukkit.event.Listener {

    private final Map<UUID, UUID> viewingPlayers;
    private final Map<UUID, Inventory> previousInventories;
    private final Set<UUID> forceOpeningPlayers = new HashSet<>();
    private final JavaPlugin plugin;

    public EventListener(Map<UUID, UUID> viewingPlayers, JavaPlugin plugin) {
        this.viewingPlayers = viewingPlayers;
        this.previousInventories = new HashMap<>();
        this.plugin = plugin;
    }



    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (forceOpeningPlayers.contains(uuid)) {
            forceOpeningPlayers.remove(uuid); // 강제로 여는 인벤토리면 무시
            return;
        }

        if (viewingPlayers.containsKey(uuid) && !previousInventories.containsKey(uuid)) {
            viewingPlayers.remove(uuid);
        } else {
            Inventory previousInventory = previousInventories.get(uuid);
            if (previousInventory != null) {
                previousInventories.remove(uuid);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && player.isValid()) {
                        player.openInventory(previousInventory);
                    }
                }, 1L);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        
        if (event.getCurrentItem() == null) {
            return;
        }

        if (!viewingPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        ItemMeta meta = clickedItem.getItemMeta();


            //데이터가 블록데이터인지
            if (meta instanceof BlockStateMeta) {
                BlockStateMeta blockStateMeta = (BlockStateMeta) clickedItem.getItemMeta();
                //데이터가 있고 셜커박스인지
                if (blockStateMeta != null && blockStateMeta.getBlockState() instanceof ShulkerBox) {


                    forceOpeningPlayers.add(player.getUniqueId());
                    previousInventories.put(player.getUniqueId(), player.getOpenInventory().getTopInventory());

                    ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                    Inventory shulkerInventory = shulkerBox.getInventory();
                    Player target = Bukkit.getPlayer(viewingPlayers.get(player.getUniqueId()));

                    String shulkerBoxMessage = plugin.getConfig().getString("seeitem.shulkerbox_message", "aa");
                    Inventory gui = Bukkit.createInventory(null, 27, shulkerBoxMessage.replace("%player%", target.getName()));

                    // 셜커 박스의 내용을 GUI에 추가
                    for (int i = 0; i < shulkerInventory.getSize(); i++) {
                        ItemStack item = shulkerInventory.getItem(i);
                        if (item != null) {
                            gui.setItem(i, item);
                        }
                    }

                    player.openInventory(gui);

                    viewingPlayers.put(player.getUniqueId(), target.getUniqueId());
                }
                else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
    }
}
