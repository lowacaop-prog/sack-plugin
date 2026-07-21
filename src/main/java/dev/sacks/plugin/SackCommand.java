package dev.sacks.plugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class SackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sacks.give")) { sender.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /sack <foraging|mining|farming|fishing|combat|digger> [player]");
            return true;
        }
        SackType type = SackType.fromString(args[0]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown sack type! Choose: foraging, mining, farming, fishing, combat, digger");
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player \"" + args[1] + "\" not found or not online.");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player: /sack <type> <player>");
                return true;
            }
            target = (Player) sender;
        }

        ItemStack sack = SackItem.create(type, target.getUniqueId());
        target.getInventory().addItem(sack);
        target.sendMessage(type.getColor() + "\u2714 You received a " + type.getDisplayName() + ChatColor.GREEN + "!");
        if (sender != target) {
            sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " a " + type.getColor() + type.getDisplayName() + ChatColor.GREEN + "!");
        }
        return true;
    }
}
