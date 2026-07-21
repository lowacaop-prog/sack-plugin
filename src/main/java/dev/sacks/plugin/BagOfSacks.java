package dev.sacks.plugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Bag of Sacks (/bas) - an 18-slot storage inventory that only ever
 * accepts sack items (any type, any quantity). Sacks stored here still
 * passively collect items, same as sacks carried in the player's inventory.
 */
public class BagOfSacks {
    public static final String TITLE = ChatColor.DARK_GRAY + "Bag of Sacks";
    public static final int SIZE = 18;

    // playerUUID -> the 18 slots of their bag (nulls for empty slots)
    private final Map<UUID, ItemStack[]> storedContents = new HashMap<>();
    private final File dataFile;
    private final FileConfiguration data;

    public BagOfSacks() {
        dataFile = new File(SacksPlugin.getInstance().getDataFolder(), "bag.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) { e.printStackTrace(); }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        loadAll();
    }

    public Inventory buildGUI(Player player) {
        BagHolder holder = new BagHolder(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, SIZE, TITLE);
        holder.setInventory(inv);
        ItemStack[] stored = storedContents.computeIfAbsent(player.getUniqueId(), k -> new ItemStack[SIZE]);
        for (int i = 0; i < SIZE; i++) {
            if (stored[i] != null) inv.setItem(i, stored[i].clone());
        }
        return inv;
    }

    /** Called when a player closes their bag GUI - persists whatever is currently in it. */
    public void saveFromInventory(UUID uuid, Inventory inv) {
        ItemStack[] contents = new ItemStack[SIZE];
        for (int i = 0; i < SIZE; i++) {
            ItemStack item = inv.getItem(i);
            contents[i] = (item == null || item.getType() == Material.AIR) ? null : item.clone();
        }
        storedContents.put(uuid, contents);
        persist(uuid);
    }

    /** Whether the player has at least one sack of this type stored in their bag. */
    public boolean hasSackOfType(UUID uuid, SackType type) {
        ItemStack[] contents = storedContents.get(uuid);
        if (contents == null) return false;
        for (ItemStack item : contents) {
            if (item != null && SackItem.getSackType(item) == type) return true;
        }
        return false;
    }

    private void persist(UUID uuid) {
        String path = uuid.toString();
        data.set(path, null); // clear previous entries for this player first
        ItemStack[] contents = storedContents.get(uuid);
        if (contents != null) {
            for (int i = 0; i < SIZE; i++) {
                if (contents[i] != null) data.set(path + "." + i, contents[i]);
            }
        }
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadAll() {
        for (String uuidStr : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ItemStack[] contents = new ItemStack[SIZE];
                ConfigurationSection section = data.getConfigurationSection(uuidStr);
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        try {
                            int slot = Integer.parseInt(key);
                            if (slot >= 0 && slot < SIZE) contents[slot] = section.getItemStack(key);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                storedContents.put(uuid, contents);
            } catch (Exception ignored) {}
        }
    }

    /** Marks an inventory as belonging to a player's Bag of Sacks, so the listener can identify it reliably. */
    public static class BagHolder implements InventoryHolder {
        private final UUID owner;
        private Inventory inventory;
        public BagHolder(UUID owner) { this.owner = owner; }
        public UUID getOwner() { return owner; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public Inventory getInventory() { return inventory; }
    }
}
