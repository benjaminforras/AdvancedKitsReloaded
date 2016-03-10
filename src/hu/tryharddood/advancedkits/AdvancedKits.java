package hu.tryharddood.advancedkits;

import hu.tryharddood.advancedkits.Commands.CommandManager;
import hu.tryharddood.advancedkits.Configuration.Configuration;
import hu.tryharddood.advancedkits.KitManager.Kit;
import hu.tryharddood.advancedkits.KitManager.KitManager;
import hu.tryharddood.advancedkits.Listeners.InventoryListener;
import hu.tryharddood.advancedkits.Listeners.SignListener;
import hu.tryharddood.advancedkits.Utils.UpdateManager;
import me.libraryaddict.inventory.InventoryApi;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdvancedKits extends JavaPlugin
{
    public static AdvancedKits instance;
    public static ConsoleCommandSender console;
    public static Economy econ = null;
    private final Logger log = this.getLogger();
    private Configuration configuration;

    public static AdvancedKits getInstance()
    {
        return instance;
    }

    public static void log(String message)
    {
        console.sendMessage("[" + AdvancedKits.getInstance().getDescription().getName() + "] " + message);
    }

    @Override
    public void onEnable()
    {
        console = getServer().getConsoleSender();
        instance = this;

        log(ChatColor.GREEN + "- Loading AdvancedKits Reloaded v" + this.getDescription().getVersion() + ".");

        this.configuration = new Configuration(this);
        this.configuration.loadConfiguration();
        KitManager.load();

        if (this.configuration.isEconomy())
        {
            setupVault(getServer().getPluginManager());
        }

        getCommand("kit").setExecutor(new CommandManager());
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryApi(), this);
        InventoryApi.setInstance(this);

        checkForUpdate();
    }

    private void checkForUpdate()
    {
        UpdateManager.check();
        if (UpdateManager.isUpdated())
        {
            log(ChatColor.RED + "Update found: v" + UpdateManager.LATEST_VERSION + "!");
            log(ChatColor.GREEN + "Download the latest here: " + UpdateManager.DOWNLOAD_LINK);
        }
        else
        {
            log(ChatColor.GREEN + "You're running the latest AdvancedKitsReloaded version.");
        }
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void onDisable()
    {
        for (Kit kit : KitManager.getKits())
        {
            try
            {
                kit.getYaml().save(kit.getSaveFile());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        log(ChatColor.GREEN + "- AdvancedKits Reloaded v" + this.getDescription().getVersion() + " successfully disabled.");
    }

    public void setupVault(PluginManager pm)
    {
        Plugin vault = pm.getPlugin("Vault");

        if ((vault != null) && (vault instanceof net.milkbowl.vault.Vault))
        {
            log.log(Level.INFO, "Vault v{0} loaded.", vault.getDescription().getVersion());

            if (!setupEconomy())
            {
                log(ChatColor.RED + "No economy plugin found!");
                getConfiguration().setEconomy(false);
            }
            else
            {
                log(ChatColor.GREEN + "Found an economy plugin. Using it.");
                log(ChatColor.GREEN + "- Economy support enabled.");
            }
        }
        else
        {
            log(ChatColor.RED + "Can't find Vault. Disabling economy support");
            getConfiguration().setEconomy(false);
        }
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

        if (economyProvider != null)
        {
            econ = economyProvider.getProvider();
        }
        else
        {
            log(ChatColor.RED + "No economy plugin found! This plugin may not work properly.");
            getConfiguration().setEconomy(false);
            return false;
        }

        return (econ != null);
    }
}