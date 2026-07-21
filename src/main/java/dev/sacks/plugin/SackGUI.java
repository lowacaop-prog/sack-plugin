package dev.sacks.plugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;
public class SackGUI {
    public static final String GUI_TITLE_PREFIX = "§8Sack: ";
    public static final NamespacedKey GUI_ITEM_KEY = new NamespacedKey(SacksPlugin.getInstance(), "sack_gui_item");

    public static Inventory build(Player player, SackType type) {
        String title = GUI_TITLE_PREFIX + type.getColor() + type.getDisplayName();
        Inventory inv = Bukkit.createInventory(null, 54, title);
        SackManager manager = SacksPlugin.getInstance().getSackManager();
        int capacity = manager.getCapacity(player, type);
        List<Material> items = new ArrayList<>(type.getItems());
        Collections.sort(items, Comparator.comparing(Material::name));
        int slot = 0;
        for (Material mat : items) {
            if (slot >= 45) break;
            int amount = manager.getAmount(player.getUniqueId(), type, mat);
            ItemStack display = new ItemStack(mat);
            ItemMeta meta = display.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + formatName(mat.name()));
            meta.getPersistentDataContainer().set(GUI_ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Stored: " + ChatColor.WHITE + amount + ChatColor.GRAY + " / " + capacity);
            lore.add("");
            if (amount > 0) {
                lore.add(ChatColor.GREEN + "Left-click to withdraw 1 stack (64)");
                lore.add(ChatColor.YELLOW + "Right-click to withdraw all");
            } else {
                lore.add(ChatColor.GRAY + "None stored");
            }
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(slot++, display);
        }
        ItemStack info = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GREEN + "Total stored: " + ChatColor.WHITE + manager.getTotalItems(player.getUniqueId(), type));
        infoMeta.getPersistentDataContainer().set(GUI_ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
        info.setItemMeta(infoMeta);
        for (int i = 45; i < 54; i++) inv.setItem(i, info);
        return inv;
    }

    public static boolean isGuiItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(GUI_ITEM_KEY, PersistentDataType.BYTE);
    }

    public static boolean isInfoItem(ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.LIME_STAINED_GLASS_PANE;
    }

    private static String formatName(String name) {
        String[] words = name.toLowerCase().replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        return sb.toString().trim();
    }
}
