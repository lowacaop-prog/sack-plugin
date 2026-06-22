package dev.sacks.plugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import java.util.*;
public class SackItem {
    public static final NamespacedKey SACK_TYPE_KEY = new NamespacedKey(SacksPlugin.getInstance(), "sack_type");
    private static final Map<SackType, String> TEXTURES = new EnumMap<>(SackType.class);
    static {
        TEXTURES.put(SackType.FORAGING, "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ2ZWQ2ODVlNzllMTJjZTA4OTE5NjI3NjE4NDc0MzU5MTQxN2U3ZjU0ZWQ3ZWZhZjk4ZDY2N2Q4ZDI0OWVlYiJ9fX0=");
        TEXTURES.put(SackType.MINING,   "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjczNjc4MDY2NDcyYTFjOWZkNWE1MzQ1ZWI3ZWIzMGIyYTNlYmVmZWE3NTA2YWM2ZTlmOWMxMTIzZTYyNzFhOSJ9fX0=");
        TEXTURES.put(SackType.DIGGER,   "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGUzYjc3ZGZhMjM1MTBjNjNiNGIxOWFhNGJiZDNkMjgwM2U5OTkzNjYzNzU2MDhmZTMyNTI0ZTQ1OWE0NWY4NSJ9fX0=");
        TEXTURES.put(SackType.COMBAT,   "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQ2M2IxZTNjZTVkZGM5MDNiNGM0ZWI3MjUxNzdiMThjYjVjMWRkOTA1MDk2ZTJmMzE3N2JhN2QyNjAyMTkxYSJ9fX0=");
        TEXTURES.put(SackType.FISHING,  "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmJkYzRjZmY2M2I4YzYzNzljNjRmOGQxMjc0YzYzMDE5MzhiMDYxM2VjZjIxMzE4ZjAxZmY2OWUyZjhlMWJkZCJ9fX0=");
        TEXTURES.put(SackType.FARMING,  "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmRhZTlmN2IyM2ZjYWE0YzQ1NTQyNzhjYmMzYzk5NjIxYTRjYmQ3MWZjMjYzYWY3ZGQyZTM5ZTUxZTVhYTRkNCJ9fX0=");
    }

    public static ItemStack create(SackType type, UUID ownerUUID) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        applyTexture(meta, type);
        meta.setDisplayName(type.getColor() + "" + ChatColor.BOLD + type.getDisplayName());
        meta.getPersistentDataContainer().set(SACK_TYPE_KEY, PersistentDataType.STRING, type.name());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Automatically collects " + type.getColor() + type.getDisplayName().replace(" Sack", "").toLowerCase() + ChatColor.GRAY + " items");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Right-click to open");
        lore.add(ChatColor.YELLOW + "Shift + Right-click to insert all");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static void applyTexture(SkullMeta meta, SackType type) {
        String textureValue = TEXTURES.get(type);
        if (textureValue == null) return;
        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "SackHead");
            profile.setProperty(new ProfileProperty("textures", textureValue));
            meta.setPlayerProfile(profile);
        } catch (Exception e) {
            SacksPlugin.getInstance().getLogger().warning("Texture failed for " + type.name() + ": " + e.getMessage());
        }
    }

    public static SackType getSackType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String typeName = item.getItemMeta().getPersistentDataContainer().get(SACK_TYPE_KEY, PersistentDataType.STRING);
        if (typeName == null) return null;
        return SackType.fromString(typeName);
    }
}
