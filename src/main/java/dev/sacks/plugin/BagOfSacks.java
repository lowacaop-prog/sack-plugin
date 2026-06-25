package dev.sacks.plugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.io.File;
import java.io.IOException;
import java.util.*;
public class BagOfSacks {
    private static final String TITLE = "§8Bag of Sacks";
    private static final NamespacedKey BAG_SLOT_KEY = new NamespacedKey(SacksPlugin.getInstance(), "bag_slot");
    // Slots where sacks go: 10, 12, 14, 16, 20, 22 (nicely spaced in 27-slot GUI)
    private static final int[] SACK_SLOTS = {10, 12, 14, 20, 22, 24};
    private static final SackType[] SACK_ORDER = {
        SackType.FORAGING, SackType.MINING, SackType.FARMING,
        SackType.FISHING, SackType.COMBAT, SackType.DIGGER
    };

    // playerUUID -> which sack types are stored in the bag
    private final Map<UUID, Set<SackType>> storedSacks = new HashMap<>();
    private final File dataFile;
    private final FileConfiguration data;

    public BagOfSacks() {
        dataFile = new File(SacksPlugin.getInstance().getDataFolder(), "bag.yml");
        if (!dataFile.exists()) try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        data = YamlConfiguration.loadConfiguration(dataFile);
        loadAll();
    }

    public boolean hasSackInBag(UUID uuid, SackType type) {
        return storedSacks.computeIfAbsent(uuid, k -> new HashSet<>()).contains(type);
    }

    public void storeSack(UUID uuid, SackType type) {
        storedSacks.computeIfAbsent(uuid, k -> new HashSet<>()).add(type);
        save(uuid);
    }

    public void removeSack(UUID uuid, SackType type) {
        storedSacks.computeIfAbsent(uuid, k -> new HashSet<>()).remove(type);
        save(uuid);
    }

    public Set<SackType> getStoredSacks(UUID uuid) {
        return storedSacks.computeIfAbsent(uuid, k -> new HashSet<>());
    }

    public Inventory buildGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        Set<SackType> stored = getStoredSacks(player.getUniqueId());

        for (int i = 0; i < SACK_ORDER.length; i++) {
            SackType type = SACK_ORDER[i];
            int slot = SACK_SLOTS[i];
            if (stored.contains(type)) {
                // Show the actual sack item
                ItemStack sack = SackItem.create(type, player.getUniqueId());
                inv.setItem(slot, sack);
            } else {
                // Show empty placeholder
                ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = placeholder.getItemMeta();
                meta.setDisplayName(type.getColor() + type.getDisplayName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "No sack stored");
                lore.add(ChatColor.GRAY + "Right-click a sack to store it here");
                meta.setLore(lore);
                meta.getPersistentDataContainer().set(BAG_SLOT_KEY, PersistentDataType.STRING, type.name());
                placeholder.setItemMeta(meta);
                inv.setItem(slot, placeholder);
            }
        }
        // Filler
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, filler);
        }
        return inv;
    }

    public static String getTitle() { return TITLE; }
    public static int[] getSackSlots() { return SACK_SLOTS; }
    public static SackType[] getSackOrder() { return SACK_ORDER; }
    public static NamespacedKey getBagSlotKey() { return BAG_SLOT_KEY; }

    private void save(UUID uuid) {
        Set<SackType> stored = storedSacks.get(uuid);
        if (stored == null) return;
        List<String> list = new ArrayList<>();
        for (SackType type : stored) list.add(type.name());
        data.set(uuid.toString(), list);
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadAll() {
        for (String uuidStr : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                List<String> list = data.getStringList(uuidStr);
                Set<SackType> stored = new HashSet<>();
                for (String s : list) {
                    try { stored.add(SackType.valueOf(s)); } catch (Exception ignored) {}
                }
                storedSacks.put(uuid, stored);
            } catch (Exception ignored) {}
        }
    }
}
