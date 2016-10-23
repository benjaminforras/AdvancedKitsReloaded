package hu.tryharddood.advancedkits;

import hu.tryharddood.advancedkits.Commands.CommandHandler;
import hu.tryharddood.advancedkits.Commands.SubCommands.*;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Kits.KitManager;
import hu.tryharddood.advancedkits.Listeners.PlayerListener;
import hu.tryharddood.advancedkits.Listeners.SignListener;
import hu.tryharddood.advancedkits.MenuBuilder.chat.ChatCommandListener;
import hu.tryharddood.advancedkits.MenuBuilder.inventory.InventoryListener;
import hu.tryharddood.advancedkits.Utils.I18n;
import hu.tryharddood.advancedkits.Utils.Metrics;
import hu.tryharddood.advancedkits.Utils.Minecraft;
import hu.tryharddood.advancedkits.Utils.Updater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class AdvancedKits extends JavaPlugin {
	public static Economy econ = null;
	public static Minecraft.Version ServerVersion;
	public static transient  AdvancedKits         instance;
	private static transient I18n                 i18n;
	private static transient KitManager           kitManager;
	private static transient Configuration        configuration;
	//private static transient MySQL                mySQL;
	private static           ConsoleCommandSender console;
	private static           String               _versionString;
	public InventoryListener   inventoryListener;
	public ChatCommandListener chatCommandListener;

	public static AdvancedKits getInstance() {
		return instance;
	}

	public static I18n getI18n() {
		return i18n;
	}

	public static Configuration getConfiguration() {
		return configuration;
	}

	public static KitManager getKitManager() {
		return kitManager;
	}

	public static void log(String message) {
		console.sendMessage("[" + AdvancedKits.getInstance().getDescription().getName() + "] " + message);
	}

	public synchronized static String getVersion() {
		if (_versionString == null)
		{
			if (Bukkit.getServer() == null)
			{
				return null;
			}
			String name = Bukkit.getServer().getClass().getPackage().getName();
			_versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
		}

		return _versionString;
	}

	@Override
	public void onDisable() {
		if (i18n != null)
		{
			i18n.onDisable();
		}

		/*if(mySQL != null)
		{
			mySQL.disconnect();
		}*/

		for (Map.Entry<String, Kit> kit : kitManager.getKits().entrySet())
		{
			try
			{
				kit.getValue().getYaml().save(kit.getValue().getSaveFile());
			} catch (IOException e)
			{
				AdvancedKits.log(ChatColor.RED + "Please send this to the author of this plugin:");
				AdvancedKits.log(" -- StackTrace --");
				e.printStackTrace();
				System.out.println(" -- End of StackTrace --");
			}
		}

		log(ChatColor.GREEN + "AdvancedKits Reloaded v" + this.getDescription().getVersion() + " successfully disabled.");
	}

	@Override
	public void onEnable() {
		console = getServer().getConsoleSender();
		instance = this;

		ServerVersion = Minecraft.Version.getVersion();

		if (ServerVersion.olderThan(Minecraft.Version.v1_8_R1))
		{
			log(ChatColor.RED + "Error loading AdvancedKits Reloaded v" + getDescription().getVersion());
			log(ChatColor.RED + "- Supported Minecraft versions are: 1.8, 1.9 and 1.10");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		Bukkit.getPluginManager().registerEvents(inventoryListener = new InventoryListener(this), this);
		getCommand("mbchat").setExecutor(chatCommandListener = new ChatCommandListener(this));

		this.registerCommands();

		log(ChatColor.GREEN + "Detected Minecraft version: " + ServerVersion.toString());
		log(ChatColor.GREEN + "- Loading AdvancedKits Reloaded v" + this.getDescription().getVersion() + ".");

		i18n = new I18n(this);
		i18n.onEnable();

		kitManager = new KitManager(this);
		kitManager.load();

		configuration = new Configuration(this);
		configuration.load();

		/*if(configuration.getSaveType().equalsIgnoreCase("mysql"))
		{
			mySQL = configuration.getMySQL();
			mySQL.connect();
		}*/

		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new SignListener(), this);

		log(ChatColor.GREEN + "- Initalizing Metrics");
		try
		{
			Metrics metrics = new Metrics(this);
			metrics.start();

			new Updater(this, 11193);
		} catch (IOException e)
		{
			log(ChatColor.RED + "- Failed to initalize Metrics");
		}
	}

	private void registerCommands() {
		getCommand("kit").setExecutor(new CommandHandler());

		CommandHandler.addComand(Collections.singletonList("use"), new UseCommand());
		CommandHandler.addComand(Collections.singletonList("buy"), new BuyCommand());
		CommandHandler.addComand(Collections.singletonList("view"), new ViewCommand());
		CommandHandler.addComand(Collections.singletonList("create"), new CreateCommand());
		CommandHandler.addComand(Collections.singletonList("edit"), new EditCommand());
		CommandHandler.addComand(Collections.singletonList("delete"), new DeleteCommand());

		CommandHandler.addComand(Collections.singletonList("give"), new GiveCommand());

		CommandHandler.addComand(Arrays.asList("setflag", "flag"), new SetFlagCommand());
		CommandHandler.addComand(Arrays.asList("edititem", "item", "setitem"), new EditItemCommand());

		CommandHandler.addComand(Collections.singletonList("reload"), new ReloadCommand());
		CommandHandler.addComand(Collections.singletonList("version"), new VersionCommand());
		CommandHandler.addComand(Collections.singletonList("help"), new HelpCommand());
		CommandHandler.addComand(Collections.singletonList("update"), new UpdateCommand());
	}

	public boolean setupEconomy() {
		try
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
		} catch (NoClassDefFoundError ex)
		{
			log(ChatColor.RED + "- No economy plugin found! This plugin may not work properly.");
			getConfiguration().setEconomy(false);
			return false;
		}
		return (econ != null);
	}

	public void setupVault(PluginManager pm) {
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
