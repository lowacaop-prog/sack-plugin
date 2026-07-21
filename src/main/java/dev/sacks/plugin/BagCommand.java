package dev.sacks.plugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class BagCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage(ChatColor.RED + "Only players can use this."); return true; }
        if (!player.hasPermission("sacks.use")) { player.sendMessage(ChatColor.RED + "No permission."); return true; }
        player.openInventory(SacksPlugin.getInstance().getBagOfSacks().buildGUI(player));
        return true;
    }
}
