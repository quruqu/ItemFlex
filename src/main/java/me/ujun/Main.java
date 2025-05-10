package me.ujun;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ObjectInputFilter;
import java.lang.module.Configuration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class Main extends JavaPlugin{

    private final Map<UUID, UUID> viewingPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();


        Command commands = new Command(viewingPlayers, this);

        getCommand("showitem").setExecutor(commands);
        getCommand("seeitem").setExecutor(commands);
        getCommand("reload-itemflex").setExecutor(new ReloadCommand(this));
        getCommand("showitem").setTabCompleter(new CommandTabCompleter());
        getCommand("seeitem").setTabCompleter(new CommandTabCompleter());
        Bukkit.getPluginManager().registerEvents(new EventListener(viewingPlayers, this), this); // 이벤트 리스너 클래스에 두 해시맵 전달


    }

    @Override
    public void onDisable() {

    }


}
