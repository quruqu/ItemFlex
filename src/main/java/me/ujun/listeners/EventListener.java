package me.ujun.listeners;

import me.ujun.utils.Storage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
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


    private final Map<UUID, Inventory> previousInventories;
    private final Set<UUID> forceOpeningPlayers = new HashSet<>();
    private final JavaPlugin plugin;

    public EventListener(JavaPlugin plugin) {
        this.previousInventories = new HashMap<>();
        this.plugin = plugin;
    }



    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (forceOpeningPlayers.contains(uuid)) {
            forceOpeningPlayers.remove(uuid); // ignore if force opening inventory
            return;
        }

        if (Storage.viewingPlayers.containsKey(uuid) && !previousInventories.containsKey(uuid)) {
            Storage.viewingPlayers.remove(uuid);
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

        if (!Storage.viewingPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        ItemMeta meta = clickedItem.getItemMeta();



            if (meta instanceof BlockStateMeta) {
                BlockStateMeta blockStateMeta = (BlockStateMeta) clickedItem.getItemMeta();

                if (blockStateMeta != null && blockStateMeta.getBlockState() instanceof ShulkerBox) {


                    forceOpeningPlayers.add(player.getUniqueId());
                    previousInventories.put(player.getUniqueId(), player.getOpenInventory().getTopInventory());

                    ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                    Inventory shulkerInventory = shulkerBox.getInventory();
                    Player target = Bukkit.getPlayer(Storage.viewingPlayers.get(player.getUniqueId()));

                    String shulkerBoxMessage = plugin.getConfig().getString("seeitem.shulkerbox_message",  target.getName() + "'s ShulkerBox");
                    Inventory gui = Bukkit.createInventory(null, 27, shulkerBoxMessage.replace("%player%", target.getName()));


                    for (int i = 0; i < shulkerInventory.getSize(); i++) {
                        ItemStack item = shulkerInventory.getItem(i);
                        if (item != null) {
                            gui.setItem(i, item);
                        }
                    }

                    player.openInventory(gui);

                    Storage.viewingPlayers.put(player.getUniqueId(), target.getUniqueId());
                }
                else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
    }
}
