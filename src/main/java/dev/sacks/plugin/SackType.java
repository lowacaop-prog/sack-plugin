package dev.sacks.plugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import java.util.*;
public enum SackType {
    FORAGING("Foraging Sack", ChatColor.GREEN, new Material[]{
        Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
        Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
        Material.MANGROVE_LOG, Material.CHERRY_LOG,
        Material.STICK, Material.APPLE, Material.COCOA_BEANS
    }),
    MINING("Mining Sack", ChatColor.GRAY, new Material[]{
        Material.COBBLESTONE, Material.STONE, Material.GRANITE, Material.DIORITE,
        Material.ANDESITE, Material.COAL, Material.IRON_ORE, Material.GOLD_ORE,
        Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.LAPIS_ORE,
        Material.REDSTONE_ORE, Material.COPPER_ORE, Material.IRON_INGOT,
        Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD,
        Material.LAPIS_LAZULI, Material.REDSTONE, Material.COPPER_INGOT,
        Material.QUARTZ, Material.NETHERRACK, Material.NETHER_QUARTZ_ORE,
        Material.OBSIDIAN, Material.DEEPSLATE, Material.COBBLED_DEEPSLATE
    }),
    FARMING("Farming Sack", ChatColor.YELLOW, new Material[]{
        Material.WHEAT, Material.WHEAT_SEEDS, Material.CARROT, Material.POTATO,
        Material.BEETROOT, Material.BEETROOT_SEEDS, Material.PUMPKIN,
        Material.MELON, Material.MELON_SLICE, Material.SUGAR_CANE,
        Material.CACTUS, Material.BAMBOO, Material.NETHER_WART,
        Material.CHORUS_FRUIT, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM,
        Material.KELP, Material.SEA_PICKLE, Material.LILY_PAD, Material.DRIED_KELP
    }),
    FISHING("Fishing Sack", ChatColor.AQUA, new Material[]{
        Material.COD, Material.SALMON, Material.TROPICAL_FISH, Material.PUFFERFISH,
        Material.COOKED_COD, Material.COOKED_SALMON,
        Material.INK_SAC, Material.GLOW_INK_SAC,
        Material.NAUTILUS_SHELL, Material.HEART_OF_THE_SEA,
        Material.FISHING_ROD, Material.STRING, Material.BONE,
        Material.LEATHER, Material.SADDLE, Material.STICK, Material.BOWL
    }),
    COMBAT("Combat Sack", ChatColor.RED, new Material[]{
        Material.ROTTEN_FLESH, Material.BONE, Material.ARROW,
        Material.GUNPOWDER, Material.STRING, Material.SPIDER_EYE,
        Material.ENDER_PEARL, Material.BLAZE_ROD, Material.BLAZE_POWDER,
        Material.GHAST_TEAR, Material.MAGMA_CREAM, Material.SLIME_BALL,
        Material.PHANTOM_MEMBRANE, Material.RABBIT_FOOT, Material.RABBIT_HIDE,
        Material.LEATHER, Material.FEATHER, Material.INK_SAC,
        Material.SHULKER_SHELL, Material.GOLD_NUGGET, Material.IRON_NUGGET
    }),
    DIGGER("Digger Sack", ChatColor.GOLD, new Material[]{
        Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT,
        Material.ROOTED_DIRT, Material.PODZOL, Material.MYCELIUM,
        Material.SAND, Material.RED_SAND, Material.GRAVEL,
        Material.SOUL_SAND, Material.SOUL_SOIL,
        Material.CLAY, Material.CLAY_BALL,
        Material.MUD, Material.MUDDY_MANGROVE_ROOTS,
        Material.MOSS_BLOCK, Material.MOSS_CARPET,
        Material.SNOW, Material.SNOW_BLOCK, Material.ICE, Material.PACKED_ICE,
        Material.SUSPICIOUS_SAND, Material.SUSPICIOUS_GRAVEL
    });
    private final String displayName;
    private final ChatColor color;
    private final Set<Material> items;
    SackType(String displayName, ChatColor color, Material[] items) {
        this.displayName = displayName;
        this.color = color;
        this.items = new HashSet<>(Arrays.asList(items));
    }
    public String getDisplayName() { return displayName; }
    public ChatColor getColor() { return color; }
    public Set<Material> getItems() { return items; }
    public boolean accepts(Material material) { return items.contains(material); }
    public static SackType fromString(String name) {
        try { return valueOf(name.toUpperCase()); } catch (IllegalArgumentException e) { return null; }
    }
}
