package tk.shanebee.bee;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import tk.shanebee.bee.api.NBTApi;
import tk.shanebee.bee.elements.board.listener.PlayerListener;

import java.io.IOException;

public class SkBee extends JavaPlugin {

    private static SkBee instance;
    private NBTApi nbtApi;
    private PluginManager pm;
    private SkriptAddon addon;

    @Override
    public void onEnable() {
        instance = this;
        this.nbtApi = new NBTApi();
        this.pm = Bukkit.getPluginManager();
        PluginDescriptionFile desc = getDescription();

        if ((pm.getPlugin("Skript") != null) && Skript.isAcceptRegistrations()) {
            addon = Skript.registerAddon(this);

            // Load Skript elements
            if (!loadNBTElements()) return;
            loadRecipeElements();
            loadBoardElements();

            // Beta check + notice
            if (desc.getVersion().contains("Beta")) {
                log("&eThis is a BETA build, things may not work as expected, please report any bugs on GitHub");
                log("&ehttps://github.com/ShaneBeee/SkBee/issues");
            }
        } else {
            log("&cDependency Skript was not found, plugin disabling");
            pm.disablePlugin(this);
            return;
        }
        log("&aSuccessfully enabled v" + desc.getVersion());
    }

    private boolean loadNBTElements() {
        try {
            addon.loadClasses("tk.shanebee.bee.elements.nbt");
            log("&5NBT Elements &asuccessfully loaded");
            nbtApi.forceLoadNBT();
        } catch (IOException ex) {
            ex.printStackTrace();
            pm.disablePlugin(this);
            return false;
        }
        return true;
    }

    private void loadRecipeElements() {
        if (Skript.isRunningMinecraft(1, 13)) {
            try {
                addon.loadClasses("tk.shanebee.bee.elements.recipe");
                log("&5Recipe Elements &asuccessfully loaded");
            } catch (IOException ex) {
                ex.printStackTrace();
                pm.disablePlugin(this);
            }
        } else {
            log("&5Recipe Elements &cdisabled");
            log("&7 - Recipe elements are only available on 1.13+");
        }
    }

    private void loadBoardElements() {
        if (Skript.isRunningMinecraft(1, 13)) {
            try {
                addon.loadClasses("tk.shanebee.bee.elements.board");
                pm.registerEvents(new PlayerListener(), this);
                log("&5Scoreboard Elements &asuccessfully loaded");
            } catch (IOException ex) {
                ex.printStackTrace();
                pm.disablePlugin(this);
            }
        } else {
            log("&5Scoreboard Elements &cdisabled");
            log("&7 - Scoreboard elements are only available on 1.13+");
        }
    }

    @Override
    public void onDisable() {
    }

    public static SkBee getPlugin() {
        return instance;
    }

    public NBTApi getNbtApi() {
        return nbtApi;
    }

    public static void log(String log) {
        String prefix = "&7[&bSk&3Bee&7] ";
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + log));
    }


}
