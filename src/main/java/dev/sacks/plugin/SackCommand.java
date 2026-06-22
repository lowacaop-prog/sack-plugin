package dev.sacks.plugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class SackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage(ChatColor.RED + "Only players can use this."); return true; }
        if (!player.hasPermission("sacks.give")) { player.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /sack <foraging|mining|farming|fishing|combat>");
            return true;
        }
        SackType type = SackType.fromString(args[0]);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Unknown sack type! Choose: foraging, mining, farming, fishing, combat");
            return true;
        }
        ItemStack sack = SackItem.create(type, player.getUniqueId());
        player.getInventory().addItem(sack);
        player.sendMessage(type.getColor() + "✔ You received a " + type.getDisplayName() + ChatColor.GREEN + "!");
        return true;
    }
}
