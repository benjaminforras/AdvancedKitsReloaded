package hu.tryharddood.advancedkits;

import hu.tryharddood.advancedkits.Commands.CommandHandler;
import hu.tryharddood.advancedkits.Commands.SubCommands.*;
import hu.tryharddood.advancedkits.Configuration.Configuration;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Kits.KitManager;
import hu.tryharddood.advancedkits.Listeners.InventoryListener;
import hu.tryharddood.advancedkits.Listeners.PlayerListener;
import hu.tryharddood.advancedkits.Listeners.SignListener;
import hu.tryharddood.advancedkits.Utils.I18n;
import hu.tryharddood.advancedkits.Utils.Metrics;
import hu.tryharddood.advancedkits.Utils.Updater;
import me.libraryaddict.inventory.InventoryApi;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class AdvancedKits extends JavaPlugin
{
    public static Economy econ = null;
    public static            Integer              ServerVersion;
    private static transient AdvancedKits         instance;
    private static transient I18n                 i18n;
    private static transient KitManager           kitManager;
    private static           ConsoleCommandSender console;
    private                  Configuration        configuration;

    public static AdvancedKits getInstance()
    {
        return instance;
    }

    public static I18n getI18n()
    {
        return i18n;
    }

    public static KitManager getKitManager()
    {
        return kitManager;
    }

    public static void log(String message)
    {
        console.sendMessage("[" + AdvancedKits.getInstance().getDescription().getName() + "] " + message);
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
        if (i18n != null)
        {
            i18n.onDisable();
        }

        for (Kit kit : kitManager.getKits())
        {
            try
            {
                kit.getYaml().save(kit.getSaveFile());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        log(ChatColor.GREEN + "AdvancedKits Reloaded v" + this.getDescription().getVersion() + " successfully disabled.");
    }

    @Override
    public void onEnable()
    {
        console = getServer().getConsoleSender();
        instance = this;

        String a       = getServer().getClass().getPackage().getName();
        String version = a.substring(a.lastIndexOf('.') + 1);
        if (version.contains("1_8"))
        {
            ServerVersion = 18;
        }
        else if (version.contains("1_9"))
        {
            ServerVersion = 19;
        }
        else
        {
            log(ChatColor.RED + "Error loading AdvancedKits Reloaded v" + this.getDescription().getVersion() + ".");
            log(ChatColor.RED + "- Supported Minecraft versions are: 1.8 and 1.9");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.registerCommands();

        log(ChatColor.GREEN + "Detected Minecraft version: " + (ServerVersion == 19 ? "1.9.X" : "1.8.X"));
        log(ChatColor.GREEN + "- Loading AdvancedKits Reloaded v" + this.getDescription().getVersion() + ".");

        i18n = new I18n(this);
        i18n.onEnable();
        i18n.updateLocale("en");

        kitManager = new KitManager(this);
        kitManager.load();

        configuration = new Configuration(this);
        configuration.loadConfiguration();

        if (configuration.isEconomy())
        {
            setupVault(getServer().getPluginManager());
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);

        if (InventoryApi.getInstance() == null)
        {
            getServer().getPluginManager().registerEvents(new InventoryApi(), this);
            InventoryApi.setInstance(this);
        }

        log(ChatColor.GREEN + "- Initalizing Metrics");
        try
        {
            Metrics metrics = new Metrics(this);
            metrics.start();

            new Updater(this, 11193);
        }
        catch (IOException e)
        {
            log(ChatColor.RED + "- Failed to initalize Metrics");
        }
    }

    private void registerCommands()
    {
        getCommand("kit").setExecutor(new CommandHandler());

        CommandHandler.addComand(Collections.singletonList("use"), new UseCommand());
        CommandHandler.addComand(Collections.singletonList("buy"), new BuyCommand());
        CommandHandler.addComand(Collections.singletonList("view"), new ViewCommand());
        CommandHandler.addComand(Collections.singletonList("create"), new CreateCommand());
        CommandHandler.addComand(Collections.singletonList("edit"), new EditCommand());
        CommandHandler.addComand(Collections.singletonList("delete"), new DeleteCommand());

        CommandHandler.addComand(Arrays.asList("setflag", "flag"), new SetFlagCommand());
        CommandHandler.addComand(Arrays.asList("edititem", "item", "setitem"), new EditItemCommand());

        CommandHandler.addComand(Collections.singletonList("reload"), new ReloadCommand());
        CommandHandler.addComand(Collections.singletonList("version"), new VersionCommand());
        CommandHandler.addComand(Collections.singletonList("help"), new HelpCommand());
        CommandHandler.addComand(Collections.singletonList("update"), new UpdateCommand());
    }

    public boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

        if (economyProvider != null)
        {
            econ = economyProvider.getProvider();
        }
        else
        {
            log(ChatColor.RED + "- No economy plugin found! This plugin may not work properly.");
            getConfiguration().setEconomy(false);
            return false;
        }

        return (econ != null);
    }

    public void setupVault(PluginManager pm)
    {
        Plugin vault = pm.getPlugin("Vault");

        if ((vault != null) && (vault instanceof net.milkbowl.vault.Vault))
        {
            log(ChatColor.GREEN + "Vault v" + vault.getDescription().getVersion() + " loaded.");

            if (!setupEconomy())
            {
                log(ChatColor.RED + "- No economy plugin found!");
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
}
