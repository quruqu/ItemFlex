package me.ujun;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ReloadCommand implements CommandExecutor {

    private Main plugin;


    public ReloadCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;


        if (command.getName().equals("reload-itemflex")) {

            player.sendMessage(plugin.getConfig().getString("reload_message", "리로드 완료"));
            plugin.reloadConfig();
        }

        return false;
    }
}
