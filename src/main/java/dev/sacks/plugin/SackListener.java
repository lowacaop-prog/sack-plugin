package dev.sacks.plugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.util.*;
public class SackListener implements Listener {
    private final SacksPlugin plugin;
    public static final Map<UUID, SackType> openSacks = new HashMap<>();
    private final Set<UUID> reopening = new HashSet<>();

    // Whitelist of the ONLY actions we process - everything else is cancelled
    private static final Set<ClickType> ALLOWED_CLICKS = new HashSet<>(Arrays.asList(
        ClickType.LEFT, ClickType.RIGHT
    ));

    public SackListener(SacksPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack drop = event.getItem().getItemStack();
        Material mat = drop.getType();
        for (SackType type : SackType.values()) {
            if (!type.accepts(mat)) continue;
            if (!hasSack(player, type)) continue;
            int added = plugin.getSackManager().addItem(player.getUniqueId(), type, mat, drop.getAmount(), getCapacity(player, type));
            if (added > 0) {
                showActionBar(player, type, mat, added);
                if (added >= drop.getAmount()) {
                    event.setCancelled(true);
                    event.getItem().remove();
                } else {
                    drop.setAmount(drop.getAmount() - added);
                    event.getItem().setItemStack(drop);
                }
            }
            break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack drop : event.getDrops()) {
            if (drop == null) continue;
            for (SackType type : SackType.values()) {
                if (!type.accepts(drop.getType())) continue;
                if (!hasSack(player, type)) continue;
                int added = plugin.getSackManager().addItem(player.getUniqueId(), type, drop.getType(), drop.getAmount(), getCapacity(player, type));
                if (added > 0) {
                    showActionBar(player, type, drop.getType(), added);
                    if (added >= drop.getAmount()) toRemove.add(drop);
                    else drop.setAmount(drop.getAmount() - added);
                }
                break;
            }
        }
        event.getDrops().removeAll(toRemove);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof org.bukkit.entity.Item caughtItem)) return;
        Player player = event.getPlayer();
        ItemStack drop = caughtItem.getItemStack();
        if (!hasSack(player, SackType.FISHING)) return;
        if (!SackType.FISHING.accepts(drop.getType())) return;
        int added = plugin.getSackManager().addItem(player.getUniqueId(), SackType.FISHING, drop.getType(), drop.getAmount(), getCapacity(player, SackType.FISHING));
        if (added > 0) {
            showActionBar(player, SackType.FISHING, drop.getType(), added);
            caughtItem.remove();
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        // Check whichever hand actually triggered the interaction - previously this always
        // read the main hand, so a sack held in the off-hand was never recognized and vanilla
        // would let you place/break it as a block.
        ItemStack item = event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND
            ? player.getInventory().getItemInOffHand()
            : player.getInventory().getItemInMainHand();
        SackType type = SackItem.getSackType(item);
        if (type == null) return;
        event.setCancelled(true);
        if (player.isSneaking()) {
            insertAll(player, type);
        } else {
            openSacks.put(player.getUniqueId(), type);
            player.openInventory(SackGUI.build(player, type));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (reopening.contains(uuid)) return;
        openSacks.remove(uuid);
    }

    // Block ALL dragging
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!openSacks.containsKey(player.getUniqueId())) return;
        if (!event.getView().getTitle().startsWith(SackGUI.GUI_TITLE_PREFIX)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        if (!openSacks.containsKey(uuid)) return;
        if (!event.getView().getTitle().startsWith(SackGUI.GUI_TITLE_PREFIX)) return;

        // Always cancel first - no exceptions
        event.setCancelled(true);

        // Clear cursor if player somehow has an item on it
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            event.getView().setCursor(null);
            player.getInventory().addItem(event.getCursor());
        }

        // Only allow plain left/right click - block shift, hotbar, drop, middle, double, etc
        if (!ALLOWED_CLICKS.contains(event.getClick())) return;

        // Only process clicks inside the GUI (top inventory), not player's own inventory
        Inventory clicked = event.getClickedInventory();
        if (clicked == null || clicked.getType() == InventoryType.PLAYER) return;

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;
        if (current.getType() == Material.LIME_STAINED_GLASS_PANE) return;
        if (!SackGUI.isGuiItem(current)) return;

        SackType type = openSacks.get(uuid);
        SackManager manager = plugin.getSackManager();
        Material mat = current.getType();
        int stored = manager.getAmount(uuid, type, mat);
        if (stored <= 0) return;

        int withdrawn = 0;
        if (event.isLeftClick()) {
            int toWithdraw = Math.min(64, stored);
            int canFit = countFit(player, mat, toWithdraw);
            if (canFit <= 0) { player.sendMessage(ChatColor.RED + "Your inventory is full!"); return; }
            if (manager.removeItem(uuid, type, mat, canFit)) {
                player.getInventory().addItem(new ItemStack(mat, canFit));
                withdrawn = canFit;
            }
        } else if (event.isRightClick()) {
            int canFit = countFit(player, mat, stored);
            if (canFit <= 0) { player.sendMessage(ChatColor.RED + "Your inventory is full!"); return; }
            if (manager.removeItem(uuid, type, mat, canFit)) {
                player.getInventory().addItem(new ItemStack(mat, canFit));
                withdrawn = canFit;
            }
        }

        if (withdrawn > 0) {
            reopening.add(uuid);
            player.closeInventory();
            Inventory fresh = SackGUI.build(player, type);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                reopening.remove(uuid);
                player.openInventory(fresh);
                openSacks.put(uuid, type);
            });
        }
    }

    private void insertAll(Player player, SackType type) {
        PlayerInventory inv = player.getInventory();
        int totalAdded = 0;
        int capacity = getCapacity(player, type);
        Map<Material, Integer> addedByMat = new LinkedHashMap<>();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (slot == null || slot.getType() == Material.AIR) continue;
            if (SackItem.getSackType(slot) != null) continue;
            if (!type.accepts(slot.getType())) continue;
            int added = plugin.getSackManager().addItem(player.getUniqueId(), type, slot.getType(), slot.getAmount(), capacity);
            if (added > 0) {
                totalAdded += added;
                addedByMat.merge(slot.getType(), added, Integer::sum);
                if (added >= slot.getAmount()) inv.setItem(i, null);
                else slot.setAmount(slot.getAmount() - added);
            }
        }
        if (totalAdded > 0) {
            player.sendMessage(type.getColor() + "[" + type.getDisplayName() + "] " + ChatColor.GREEN + "Inserted " + totalAdded + " items:");
            for (Map.Entry<Material, Integer> entry : addedByMat.entrySet()) {
                player.sendMessage(ChatColor.GRAY + "  +" + entry.getValue() + " " + formatName(entry.getKey().name()));
            }
        } else {
            player.sendMessage(type.getColor() + "[" + type.getDisplayName() + "] " + ChatColor.RED + "No matching items in your inventory!");
        }
    }

    // Bag of Sacks: an 18-slot free-form inventory that only ever accepts sack items.
    // We don't lock every click down like the withdrawal GUI does - players can freely
    // rearrange, shift-click, and drag sacks around - we just block anything non-sack
    // from ever landing inside the top (bag) inventory.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBagClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof BagOfSacks.BagHolder)) return;

        Inventory clickedInv = event.getClickedInventory();
        InventoryAction action = event.getAction();

        switch (action) {
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
            case SWAP_WITH_CURSOR: {
                if (clickedInv == top) {
                    ItemStack cursor = event.getCursor();
                    if (cursor != null && cursor.getType() != Material.AIR && SackItem.getSackType(cursor) == null) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Only sacks can go in the bag!");
                    }
                }
                break;
            }
            case MOVE_TO_OTHER_INVENTORY: {
                // Shift-click FROM the player's own inventory INTO the bag
                if (clickedInv != null && clickedInv != top) {
                    ItemStack current = event.getCurrentItem();
                    if (current != null && SackItem.getSackType(current) == null) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Only sacks can go in the bag!");
                    }
                }
                break;
            }
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD: {
                if (clickedInv == top) {
                    ItemStack incoming;
                    if (event.getClick() == ClickType.SWAP_OFFHAND) {
                        // Pressing F over a bag slot swaps with the off-hand item, but
                        // getHotbarButton() returns -1 for this click type, so it was
                        // never being checked - letting any off-hand item into the bag.
                        incoming = player.getInventory().getItemInOffHand();
                    } else {
                        int hotbarButton = event.getHotbarButton();
                        incoming = hotbarButton >= 0 ? player.getInventory().getItem(hotbarButton) : null;
                    }
                    if (incoming != null && incoming.getType() != Material.AIR && SackItem.getSackType(incoming) == null) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Only sacks can go in the bag!");
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBagDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof BagOfSacks.BagHolder)) return;
        if (SackItem.getSackType(event.getOldCursor()) != null) return;
        int topSize = top.getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Only sacks can go in the bag!");
                return;
            }
        }
    }

    @EventHandler
    public void onBagClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof BagOfSacks.BagHolder)) return;
        SacksPlugin.getInstance().getBagOfSacks().saveFromInventory(player.getUniqueId(), event.getInventory());
    }

    private void showActionBar(Player player, SackType type, Material mat, int amount) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
            new net.md_5.bungee.api.chat.TextComponent(
                type.getColor() + "[" + type.getDisplayName() + "] " + ChatColor.WHITE + "+" + amount + " " + formatName(mat.name())));
    }
    private boolean hasSack(Player player, SackType type) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (SackItem.getSackType(item) == type) return true;
        }
        // Sacks stored in the Bag of Sacks still passively collect
        return SacksPlugin.getInstance().getBagOfSacks().hasSackOfType(player.getUniqueId(), type);
    }
    private int getCapacity(Player player, SackType type) {
        return plugin.getSackManager().getCapacity(player, type);
    }
    private int countFit(Player player, Material mat, int wanted) {
        int space = 0;
        for (ItemStack slot : player.getInventory().getStorageContents()) {
            if (slot == null) space += mat.getMaxStackSize();
            else if (slot.getType() == mat) space += mat.getMaxStackSize() - slot.getAmount();
            if (space >= wanted) return wanted;
        }
        return Math.min(space, wanted);
    }
    private String formatName(String name) {
        String[] words = name.toLowerCase().replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        return sb.toString().trim();
    }
}
