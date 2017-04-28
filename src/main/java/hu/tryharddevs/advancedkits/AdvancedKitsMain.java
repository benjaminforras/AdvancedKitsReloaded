package hu.tryharddevs.advancedkits;

import co.aikar.commands.ACF;
import co.aikar.commands.CommandManager;
import hu.tryharddevs.advancedkits.commands.MainCommand;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.flags.DefaultFlags;
import hu.tryharddevs.advancedkits.kits.flags.Flag;
import hu.tryharddevs.advancedkits.listeners.PlayerListener;
import hu.tryharddevs.advancedkits.utils.VaultUtil;
import hu.tryharddevs.advancedkits.utils.localization.I18n;
import hu.tryharddevs.advancedkits.utils.menuapi.core.MenuAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public final class AdvancedKitsMain extends JavaPlugin
{
	public static AdvancedKitsMain advancedKits;
	public        I18n             i18n;

	public static Boolean usePlaceholderAPI = false;

	public String  chatPrefix = ChatColor.translateAlternateColorCodes('&', "&7[&6AdvancedKits&7]");
	public Boolean coloredLog = true;
	public String  locale     = "en";

	private PluginDescriptionFile descriptionFile = getDescription();

	@Override
	public void onEnable()
	{
		log(ChatColor.GREEN + "Starting " + descriptionFile.getName() + " " + descriptionFile.getVersion());
		advancedKits = this;

		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			log(ChatColor.RED + "ERROR: Couldn't find necessary dependency: " + "Vault");
			setEnabled(false);
			return;
		}

		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			usePlaceholderAPI = true;
			log(ChatColor.GREEN + "Successfully hooked to PlaceholderAPI!");
		}

		log("Loading configuration.");
		loadConfiguration();
		log("Done loading the configuration.");

		log("Hooking to Vault.");
		{
			VaultUtil.loadVault();
		}
		log("Successfully hooked Vault.");

		log("Loading MenuAPI by ColonelHedgehog.");
		{
			MenuAPI menuAPI = new MenuAPI();
			menuAPI.onEnable();
		}
		log("Finished loading MenuAPI by ColonelHedgehog.");

		log("Loading KitManager.");
		{
			KitManager.loadKits();
		}
		log("Done loading KitManager.");

		log("Registering listeners");
		{
			getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		}
		log("Done registering listeners");

		log("Loading commands.");
		{
			loadCommands();
		}
		log("Finished loading commands.");

		log("Checking for updates.");
		new Updater(this, 91129, this.getFile(), Updater.UpdateType.DEFAULT, true);

		log("Loading metrics.");
		new MetricsLite(this);

		log(ChatColor.GREEN + "Finished loading " + descriptionFile.getName() + " " + descriptionFile.getVersion() + " by " + descriptionFile.getAuthors().stream().collect(Collectors.joining(",")));
	}

	public void loadConfiguration()
	{
		saveDefaultConfig();
		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "config.yml"));

		locale = configuration.getString("Locale");
		coloredLog = configuration.getBoolean("Log.ColoredLog");
		chatPrefix = ChatColor.translateAlternateColorCodes('&', configuration.getString("Chat.Prefix"));

		String localeFile = "messages_" + locale + ".properties";
		if (Objects.isNull(getResource(localeFile))) {
			log(ChatColor.RED + "Locale not found, revert back to the default. (en)");

			locale = "en";
			localeFile = "messages_" + locale + ".properties";
		}

		if (!new File(getDataFolder() + File.separator + localeFile).exists()) {
			saveResource(localeFile, false);
		}

		i18n = new I18n(this);
		i18n.onEnable();
		i18n.updateLocale(locale);
	}

	@Override
	public void onDisable()
	{
		log(ChatColor.GOLD + "Stopping " + descriptionFile.getName() + " " + descriptionFile.getVersion());
		if (i18n != null) {
			i18n.onDisable();
		}
		//log(ChatColor.GOLD + "Saving kit files.");
		//KitManager.getKits().forEach(Kit::save);
	}

	private void loadCommands()
	{
		CommandManager manager = ACF.createManager(this);

		manager.getCommandContexts().registerContext(Flag.class, Flag.getContextResolver());
		manager.getCommandContexts().registerContext(Kit.class, Kit.getContextResolver());

		manager.getCommandCompletions().registerCompletion("flags", (sender, completionConfig, input) -> (
				Arrays.stream(DefaultFlags.getFlags()).map(Flag::getName).sorted(String::compareToIgnoreCase).collect(Collectors.toCollection(ArrayList::new))
		));
		manager.getCommandCompletions().registerCompletion("kits", (sender, completionConfig, input) -> (
				KitManager.getKits().stream().map(Kit::getName).sorted(String::compareToIgnoreCase).collect(Collectors.toCollection(ArrayList::new))
		));

		manager.registerCommand(new MainCommand());

	}

	public void log(String log)
	{
		Bukkit.getConsoleSender().sendMessage(coloredLog ? chatPrefix + ChatColor.RESET + " " + log : ChatColor.stripColor(chatPrefix + ChatColor.RESET + " " + log));
	}
}
