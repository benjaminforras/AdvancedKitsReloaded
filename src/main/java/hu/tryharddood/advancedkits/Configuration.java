package hu.tryharddood.advancedkits;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Configuration {
	private static String  chatPrefix;
	private static String  locale;
	private static Boolean economy;
	private static Boolean useOnBuy;
	private static File langFile = null;
	private final AdvancedKits instance;
	private       String       savetype;
	private       MySQL        _mysql;

	public Configuration(AdvancedKits instance) {
		this.instance = instance;
	}

	public static void copyLangConfig() {
		String localeFileName = "messages_" + locale + ".properties";
		try
		{
			langFile = new File(AdvancedKits.getInstance().getDataFolder(), localeFileName);
			if (!langFile.exists())
			{
				AdvancedKits.getInstance().saveResource(localeFileName, false);
			}
		} catch (IllegalArgumentException iaex)
		{
			langFile = new File(AdvancedKits.getInstance().getDataFolder(), "messages_" + "en" + ".properties");
			if (!langFile.exists())
			{
				AdvancedKits.getInstance().saveResource("messages_" + "en" + ".properties", false);
			}
			locale = "en";
			AdvancedKits.log(ChatColor.RED + "Wrong translation file setup, using \"en\"");
		}
		AdvancedKits.getI18n().updateLocale(locale);
	}

	public Boolean getUseOnBuy() {
		return useOnBuy;
	}

	public String getChatPrefix() {
		return ChatColor.translateAlternateColorCodes('&', chatPrefix);
	}

	public String getSaveType() {return savetype;}

	public String getLocale() {
		return locale;
	}

	public boolean isEconomy() {
		return economy;
	}

	public void setEconomy(Boolean value) {
		economy = value;
	}

	private void writeChatPrefix(BufferedWriter out) throws IOException {
		out.newLine();
		out.write("# You can change the prefix in game, so it's looking more unique.");
		out.newLine();
		out.write("chat-prefix: \"&7[&6AdvancedKits&7]\"");
		out.newLine();
	}

	private void writeUseOnBuy(BufferedWriter out) throws IOException {
		out.newLine();
		out.write("# If this option is true, the player who buys a kit will automatically use it.");
		out.newLine();
		out.write("use-on-buy: false");
		out.newLine();
	}

	private void writeUseEconomy(BufferedWriter out) throws IOException {
		out.newLine();
		out.write("# You can enable the economy hook via Vault");
		out.newLine();
		out.write("# You need an Economy plugin, like Essentials");
		out.newLine();
		out.write("use-economy: true");
		out.newLine();
	}

	private void writeLocale(BufferedWriter out) throws IOException {
		out.write("# AdvancedKitsReloaded supports multiple languages.");
		out.newLine();
		out.write("# Currently there are the available values: en, hu, fi, pt, it, ru");
		out.newLine();
		out.write("# If you'd like to translate it here:");
		out.newLine();
		out.write("# https://ackuna.com/translate-/advancedkits-reloaded/");
		out.newLine();
		out.write("locale: en");
		out.newLine();
	}

	public void load() {
		try
		{
			loadConfiguration();
		} catch (IOException e)
		{
			AdvancedKits.log(ChatColor.RED + "Please send this to the author of this plugin:");
			AdvancedKits.log(" -- StackTrace --");
			e.printStackTrace();
			System.out.println(" -- End of StackTrace --");
		}
	}

	private void loadConfiguration() throws IOException {
		File dataFolder = instance.getDataFolder();
		if (!dataFolder.exists())
		{
			dataFolder.mkdir();
		}

		File configFile = new File(dataFolder, "config.yml");
		if (!configFile.exists())
		{
			configFile.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(configFile));
			out.write("# +----------------------------------------------------+");
			out.newLine();
			out.write("# <               AdvancedKits Reloaded                >");
			out.newLine();
			out.write("# <                 Configuration file                 >");
			out.newLine();
			out.write("# <                     config.yml                     >");
			out.newLine();
			out.write("# +----------------------------------------------------+");
			out.newLine();
			writeChatPrefix(out);
			writeUseEconomy(out);
			writeLocale(out);
			out.close();
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		BufferedWriter    out    = new BufferedWriter(new FileWriter(configFile, true));

		if (config.contains("chat-prefix"))
		{
			chatPrefix = config.getString("chat-prefix", "&7[&6AdvancedKits&7]");
		}
		else
		{
			writeChatPrefix(out);
		}

		if (config.contains("use-economy"))
		{
			economy = config.getBoolean("use-economy", false);
		}
		else
		{
			writeUseEconomy(out);
		}

		if (config.contains("use-on-buy"))
		{
			useOnBuy = config.getBoolean("use-on-buy", false);
		}
		else
		{
			writeUseOnBuy(out);
		}

		if (config.contains("locale"))
		{
			locale = config.getString("locale", "en");
		}
		else
		{
			writeLocale(out);
		}

		if (config.contains("savetype"))
		{
			savetype = config.getString("savetype", "file");
		}
		else
		{
			writeSaveType(out);
		}

		if (config.contains("MySQL"))
		{
			_mysql = new MySQL(config.getString("MySQL.host"), config.getInt("MySQL.port"), config.getString("MySQL.database"), config.getString("MySQL.username"), config.getString("MySQL.password"));
		}
		else
		{
			writeMysqlDatas(out);
		}
		out.close();

		if (isEconomy() && !instance.setupEconomy())
		{
			instance.setupVault(instance.getServer().getPluginManager());
		}
		copyLangConfig();
	}

	private void writeMysqlDatas(BufferedWriter out) throws IOException {
		out.write("# Mysql connection datas:");
		out.newLine();
		out.write("MySQL:\n" +
		          "  host: \"127.0.0.1\"\n" +
		          "  port: 3306\n" +
		          "  database: \"advancedkits\"\n" +
		          "  username: \"root\"\n" +
		          "  password: \"password\"\n");
		out.newLine();
	}

	private void writeSaveType(BufferedWriter out) throws IOException {
		out.write("# SaveType:");
		out.newLine();
		out.write("# MySQL or File");
		out.newLine();
		out.write("savetype: file");
		out.newLine();
	}

	public MySQL getMySQL() {
		return _mysql;
	}
}
