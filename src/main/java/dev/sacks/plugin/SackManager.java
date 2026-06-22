package dev.sacks.plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Material;
import java.io.File;
import java.io.IOException;
import java.util.*;
public class SackManager {
    private final SacksPlugin plugin;
    private File dataFile;
    private FileConfiguration data;
    // playerUUID -> sackType -> material -> amount
    private final Map<UUID, Map<SackType, Map<Material, Integer>>> sackData = new HashMap<>();
    public static final int MAX_PER_ITEM = 10000;
    public SackManager(SacksPlugin plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "sacks.yml");
        if (!dataFile.exists()) { plugin.getDataFolder().mkdirs(); try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); } }
        data = YamlConfiguration.loadConfiguration(dataFile);
        loadAll();
    }
    public Map<Material, Integer> getSack(UUID uuid, SackType type) {
        return sackData.computeIfAbsent(uuid, k -> new HashMap<>()).computeIfAbsent(type, k -> new HashMap<>());
    }
    public int getAmount(UUID uuid, SackType type, Material material) {
        return getSack(uuid, type).getOrDefault(material, 0);
    }
    public int getTotalItems(UUID uuid, SackType type) {
        return getSack(uuid, type).values().stream().mapToInt(Integer::intValue).sum();
    }
    // Returns how many were actually added
    public int addItem(UUID uuid, SackType type, Material material, int amount) {
        Map<Material, Integer> sack = getSack(uuid, type);
        int current = sack.getOrDefault(material, 0);
        int canAdd = Math.min(amount, MAX_PER_ITEM - current);
        if (canAdd <= 0) return 0;
        sack.put(material, current + canAdd);
        save(uuid);
        return canAdd;
    }
    public boolean removeItem(UUID uuid, SackType type, Material material, int amount) {
        Map<Material, Integer> sack = getSack(uuid, type);
        int current = sack.getOrDefault(material, 0);
        if (current < amount) return false;
        if (current - amount == 0) sack.remove(material);
        else sack.put(material, current - amount);
        save(uuid);
        return true;
    }
    public void save(UUID uuid) {
        Map<SackType, Map<Material, Integer>> playerData = sackData.get(uuid);
        if (playerData == null) return;
        for (SackType type : SackType.values()) {
            Map<Material, Integer> sack = playerData.get(type);
            if (sack == null) continue;
            for (Map.Entry<Material, Integer> entry : sack.entrySet()) {
                data.set(uuid + "." + type.name() + "." + entry.getKey().name(), entry.getValue());
            }
        }
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }
    private void loadAll() {
        if (data.getKeys(false).isEmpty()) return;
        for (String uuidStr : data.getKeys(false)) {
            UUID uuid;
            try { uuid = UUID.fromString(uuidStr); } catch (Exception e) { continue; }
            for (SackType type : SackType.values()) {
                if (!data.contains(uuidStr + "." + type.name())) continue;
                Map<Material, Integer> sack = getSack(uuid, type);
                for (String matName : data.getConfigurationSection(uuidStr + "." + type.name()).getKeys(false)) {
                    try {
                        Material mat = Material.valueOf(matName);
                        int amount = data.getInt(uuidStr + "." + type.name() + "." + matName);
                        sack.put(mat, amount);
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
