package dev.sacks.plugin;
import org.bukkit.plugin.java.JavaPlugin;
public class SacksPlugin extends JavaPlugin {
    private static SacksPlugin instance;
    private SackManager sackManager;
    private BagOfSacks bagOfSacks;
    @Override
    public void onEnable() {
        instance = this;
        sackManager = new SackManager(this);
        bagOfSacks = new BagOfSacks();
        getServer().getPluginManager().registerEvents(new SackListener(this), this);
        getCommand("sack").setExecutor(new SackCommand());
        getCommand("bas").setExecutor(new BagCommand());
        getLogger().info("Sacks enabled!");
    }
    @Override
    public void onDisable() {
        getLogger().info("Sacks disabled.");
    }
    public static SacksPlugin getInstance() { return instance; }
    public SackManager getSackManager() { return sackManager; }
    public BagOfSacks getBagOfSacks() { return bagOfSacks; }
}
