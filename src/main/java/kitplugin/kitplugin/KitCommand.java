package kitplugin.kitplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KitCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return true;
        switch (args[0]){
            case "add" -> FileManager.addKit(player, args[1]);
            case "remove" -> FileManager.delKit(player, args[1]);
            case "load" -> {
                ItemStack[] items = FileManager.loadKit(player, args[1]);
                player.getInventory().setContents(items);
                player.updateInventory();
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<String> strings = new ArrayList<>();
        if(!(sender instanceof Player)) return strings;
        switch (args.length){
            case 1 -> {
                strings.addAll(Arrays.asList("add", "remove", "load"));
            }
            case 2 -> {
                if(Arrays.asList("remove", "load").contains(args[1])){
                    strings.addAll(List.of(FileManager.kitList(((Player) sender))));
                }
            }
        }
        return strings;
    }
}
