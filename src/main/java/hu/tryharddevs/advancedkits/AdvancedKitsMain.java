package hu.tryharddevs.advancedkits;

import co.aikar.commands.ACF;
import co.aikar.commands.CommandManager;
import hu.tryharddevs.advancedkits.cinventory.CInventoryMain;
import hu.tryharddevs.advancedkits.commands.MainCommand;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.flags.DefaultFlags;
import hu.tryharddevs.advancedkits.kits.flags.Flag;
import hu.tryharddevs.advancedkits.listeners.PlayerListener;
import hu.tryharddevs.advancedkits.utils.VaultUtil;
import hu.tryharddevs.advancedkits.utils.localization.I18n;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class AdvancedKitsMain extends JavaPlugin {
	private static AdvancedKitsMain advancedKits;

	public I18n i18n;

	private VaultUtil      vaultUtil;
	private KitManager     kitManager;
	private CommandManager commandManager;

	private Boolean usePlaceholderAPI = false;

	public static AdvancedKitsMain getPlugin() {
		return advancedKits;
	}

	@Override
	public void onEnable() {
		this.log(ChatColor.GREEN + "Starting " + this.getDescription().getName() + " " + this.getDescription().getVersion());

		if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
			this.log(ChatColor.RED + "ERROR: Unsupported Minecraft version. (" + Minecraft.VERSION.toString() + ")");
			this.setEnabled(false);
			return;
		}

		if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
			this.log(ChatColor.RED + "ERROR: Couldn't find necessary dependency: " + "Vault");
			this.setEnabled(false);
			return;
		}
		advancedKits = this;

		this.usePlaceholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

		// Loading configuration
		this.log("Loading configuration.");
		this.saveDefaultConfig();
		Config.loadConfigurationValues(this);

		// Hooking vault
		this.log("Hooking to Vault.");
		this.vaultUtil = new VaultUtil(this);
		this.vaultUtil.hookVault();

		// Load KitManager and the kits
		this.log("Loading KitManager.");
		this.kitManager = new KitManager(this);
		this.kitManager.loadKits();

		// Register events
		this.log("Registering events");
		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		this.getServer().getPluginManager().registerEvents(new CInventoryMain(this), this);

		// Register CommandManager and the Commands.
		this.log("Registering commands.");
		this.commandManager = ACF.createManager(this);

		this.commandManager.getCommandContexts().registerContext(Flag.class, Flag.getContextResolver());
		this.commandManager.getCommandContexts().registerContext(Kit.class, Kit.getContextResolver());

		this.commandManager.getCommandCompletions().registerCompletion("flags", (sender, config, input, c) -> (Arrays.stream(DefaultFlags.getFlags()).map(Flag::getName).sorted(String::compareToIgnoreCase).collect(Collectors.toCollection(ArrayList::new))));
		this.commandManager.getCommandCompletions().registerCompletion("kits", (sender, config, input, c) -> (KitManager.getKits().stream().map(Kit::getName).sorted(String::compareToIgnoreCase).collect(Collectors.toCollection(ArrayList::new))));

		this.commandManager.registerCommand(new MainCommand(this));

		// Check for update
		if (Config.AUTOUPDATE_ENABLED) {
			this.log("Checking for updates.");
			new Updater(this, 91129, this.getFile(), Updater.UpdateType.DEFAULT, true);
		}

		// Check if metrics is enabled
		if (Config.METRICS_ENABLED) {
			log("Enabling Plugin Metrics.");
			new MetricsLite(this);
		}

		this.log(ChatColor.GREEN + "Finished loading " + this.getDescription().getName() + " " + this.getDescription().getVersion() + " by " + this.getDescription().getAuthors().stream().collect(Collectors.joining(",")));
	}

	@Override
	public void onDisable() {
		this.log(ChatColor.GOLD + "Stopping " + this.getDescription().getName() + " " + this.getDescription().getVersion());
		if (this.i18n != null) {
			this.i18n.onDisable();
		}
	}

	public void log(String log) {
		this.getServer().getConsoleSender().sendMessage(Config.COLORED_LOG ? Config.CHAT_PREFIX + ChatColor.RESET + " " + log : ChatColor.stripColor(Config.CHAT_PREFIX + ChatColor.RESET + " " + log));
	}

	public boolean isPlaceholderAPIEnabled() {
		return this.usePlaceholderAPI;
	}

	public KitManager getKitManager() {
		return this.kitManager;
	}
}
