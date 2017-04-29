package hu.tryharddevs.advancedkits;

import hu.tryharddevs.advancedkits.utils.localization.I18n;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Objects;

public class Config {
	public static String  CHAT_PREFIX        = ChatColor.translateAlternateColorCodes('&', "&7[&6AdvancedKits&7]");
	public static String  LOCALE             = "en";
	public static Boolean COLORED_LOG        = true;
	public static Boolean METRICS_ENABLED    = true;
	public static Boolean TITLES_ENABLED     = true;
	public static Boolean ACTIONBARS_ENABLED = true;

	public static void loadConfigurationValues(AdvancedKitsMain instance) {
		FileConfiguration config = instance.getConfig();

		boolean oldConfig = false;


		if(!config.contains("Chat.Prefix"))
		{
			config.addDefault("Chat.Prefix", "&7[&6AdvancedKits&7]");
			//config.addDefault("Chat.Prefix2", "&7[&6AdvancedKits&7]"); //Don't ask why. Idk
			oldConfig = true;
		}

		if(!config.contains("Locale"))
		{
			config.addDefault("Locale", "en");
			oldConfig = true;
		}

		if(!config.contains("Log.ColoredLog"))
		{
			config.addDefault("Log.ColoredLog", true);
			//config.addDefault("Log.ColoredLog2", true); //Don't ask why. Idk
			oldConfig = true;
		}

		if(!config.contains("MetricsEnabled"))
		{
			config.addDefault("MetricsEnabled", true);
			oldConfig = true;
		}

		if(!config.contains("Messages.TitlesEnabled"))
		{
			config.addDefault("Messages.TitlesEnabled", true);
			oldConfig = true;
		}

		if(!config.contains("Messages.ActionbarsEnabled"))
		{
			config.addDefault("Messages.ActionbarsEnabled", true);
			oldConfig = true;
		}

		if(oldConfig)
		{
			instance.log("Old configuration file found.. Replacing with the newer one.");

			config.set("use-economy", null);
			config.set("use-on-buy", null);
			config.set("chat-prefix", null);
			config.set("locale", null);

			config.options().copyDefaults(true);
			instance.saveConfig();
		}

		CHAT_PREFIX = ChatColor.translateAlternateColorCodes('&', config.getString("Chat.Prefix"));
		LOCALE = config.getString("Locale");

		COLORED_LOG = config.getBoolean("Log.ColoredLog");
		METRICS_ENABLED = config.getBoolean("MetricsEnabled");

		TITLES_ENABLED = config.getBoolean("Messages.TitlesEnabled", true);
		ACTIONBARS_ENABLED = config.getBoolean("Messages.ActionbarsEnabled", true);

		// Copy translations.
		String localeFile = "messages_" + LOCALE + ".properties";
		if (Objects.isNull(instance.getResource(localeFile))) {
			instance.log(ChatColor.RED + "Locale not found, revert back to the default. (en)");

			LOCALE = "en";
			localeFile = "messages_" + LOCALE + ".properties";
		}

		if (!new File(instance.getDataFolder() + File.separator + localeFile).exists()) {
			instance.saveResource(localeFile, false);
		}

		instance.i18n = new I18n(instance);
		instance.i18n.onEnable();
		instance.i18n.updateLocale(LOCALE);
	}
}
