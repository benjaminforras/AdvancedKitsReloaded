package hu.tryharddood.advancedkits.Utils;

import hu.tryharddood.advancedkits.AdvancedKits;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater extends Thread {
	private final Plugin  plugin;
	private final int     id;
	private final boolean log;
	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private URL url;

	public Updater(Plugin plugin, int resourceID) throws IOException {
		this(plugin, resourceID, true);
	}

	public Updater(Plugin plugin, int resourceID, boolean log) throws IOException {
		if (plugin == null)
		{
			throw new IllegalArgumentException("Plugin cannot be null");
		}
		if (resourceID == 0)
		{
			throw new IllegalArgumentException("Resource ID cannot be null (0)");
		}
		this.plugin = plugin;
		id = resourceID;
		this.log = log;
		url = new URL("https://api.inventivetalent.org/spigot/resource-simple/" + resourceID);

		super.start();
	}

	public synchronized void start() {
	}

	public void run() {
		if (!plugin.isEnabled())
		{
			return;
		}
		if (log)
		{
			plugin.getLogger().info("Searching for updates...");
		}
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			connection.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String content = "";
			String line;
			while ((line = in.readLine()) != null)
			{
				content = content + line;
			}
			in.close();

			JSONObject json = null;
			try
			{
				json = (JSONObject) new JSONParser().parse(content);
			} catch (ParseException ignored)
			{
			}
			String currentVersion = null;
			if ((json != null) && (json.containsKey("version")))
			{
				String version = (String) json.get("version");
				if ((version != null) && (!version.isEmpty()))
				{
					currentVersion = version;
				}
			}
			if (currentVersion == null)
			{
				if (log)
				{
					plugin.getLogger().warning("Response received.");
					plugin.getLogger().warning("Either the author of this plugin has configured the updater wrong, or the API is experiencing some issues.");
				}
				return;
			}
			if (isUpdated(currentVersion, plugin.getDescription().getVersion()))
			{
				console.sendMessage(LineUtils.line);
				console.sendMessage(LineUtils.newline);
				console.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GOLD + "" + ChatColor.BOLD + "New version found!"));
				console.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GRAY + "New version: " + ChatColor.GREEN + currentVersion + ChatColor.GRAY + " | Your version: " + ChatColor.RED + plugin.getDescription().getVersion()));
				console.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GRAY + "Get the latest version from here: "));
				console.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GRAY + "http://www.spigotmc.org/resources/" + id));
				console.sendMessage(LineUtils.newline);
				console.sendMessage(LineUtils.line);
			}
			else if (log)
			{
				console.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GREEN + plugin.getDescription().getName() + " is up-to-date."));
			}
		} catch (IOException e)
		{
			if (log)
			{
				if (connection != null)
				{
					try
					{
						int code = connection.getResponseCode();
						plugin.getLogger().warning("API connection returned response code " + code);
					} catch (IOException ignored)
					{
					}
				}
				AdvancedKits.log(ChatColor.RED + "Please send this to the author of this plugin:");
				AdvancedKits.log(" -- StackTrace --");
				e.printStackTrace();
				System.out.println(" -- End of StackTrace --");
			}
		}
	}

	private boolean isUpdated(String latestversion, String currentversion) {
		int currentVersion = 0;
		int latestVersion  = 0;
		try
		{
			currentVersion = Integer.parseInt(currentversion.replaceAll("\\.", ""));
			latestVersion = Integer.parseInt(latestversion.replaceAll("\\.", ""));
		} catch (Exception ignored)
		{
		}
		return currentVersion < latestVersion;
	}
}
