package me.ujun;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("showitem")) {
            if (args.length == 1) {
                List<String> options = Arrays.asList("mainhand", "inventory", "enderchest");
                for (String option : options) {
                    if (option.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(option);
                    }
                }
            }
        }

        if (command.getName().equalsIgnoreCase("seeitem")) {
            if (args.length == 1) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    String name = onlinePlayer.getName();
                    if (name.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(name);
                    }
                }
            } else if (args.length == 2) {
                List<String> options = Arrays.asList("mainhand", "inventory", "enderchest");
                for (String option : options) {
                    if (option.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(option);
                    }
                }
            }
        }

        return completions;
    }


}
