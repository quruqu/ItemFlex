package me.ujun;

import me.ujun.commands.CommandTabCompleter;
import me.ujun.commands.ReloadCMD;
import me.ujun.commands.SeeItemCMD;
import me.ujun.commands.ShowitemCMD;
import me.ujun.listeners.EventListener;
import me.ujun.utils.Storage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class ItemFlexPlugin extends JavaPlugin{

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("showitem").setExecutor(new ShowitemCMD(this));
        getCommand("seeitem").setExecutor(new SeeItemCMD(this));
        getCommand("reload-itemflex").setExecutor(new ReloadCMD(this));
        getCommand("showitem").setTabCompleter(new CommandTabCompleter());
        getCommand("seeitem").setTabCompleter(new CommandTabCompleter());
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);

        Storage.init(this);

        getLogger().info("Plugin Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Disabled");
    }


}
