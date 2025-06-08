package me.ujun.commands;

import me.ujun.ItemFlexPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ReloadCMD implements CommandExecutor {

    private final ItemFlexPlugin plugin;


    public ReloadCMD(ItemFlexPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;


        if (command.getName().equals("reload-itemflex")) {

            player.sendMessage(plugin.getConfig().getString("reload_message", "reload complete!"));
            plugin.reloadConfig();
        }

        return false;
    }
}
