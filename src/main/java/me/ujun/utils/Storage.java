package me.ujun.utils;

import me.ujun.ItemFlexPlugin;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Storage {

    public static final Map<UUID, ItemStack> flexedHandItem = new HashMap<>();
    public static final Map<UUID, ItemStack[]> flexedInventory = new HashMap<>();
    public static final Map<UUID, ItemStack[]> flexedEnderChest = new HashMap<>();
    public static Map<UUID, UUID> viewingPlayers = new HashMap<>();

    private static ItemFlexPlugin plugin;

    public static void init(ItemFlexPlugin pluginInstance) {
        plugin = pluginInstance;
    }


}
