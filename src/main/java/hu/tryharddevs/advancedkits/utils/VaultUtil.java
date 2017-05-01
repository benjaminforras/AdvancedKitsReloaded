package hu.tryharddevs.advancedkits.utils;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultUtil {
	private static Economy econ = null;
	private AdvancedKitsMain instance;

	public VaultUtil(AdvancedKitsMain instance) {
		this.instance = instance;
	}

	public static Economy getEconomy() {
		return econ;
	}

	public void hookVault() {
		if (!setupEconomy()) {
			this.instance.log(ChatColor.RED + String.format("[%s] - Disabled due to no Vault dependency found!", instance.getDescription().getName()));
			this.instance.getServer().getPluginManager().disablePlugin(instance);
			return;
		}
	}

	private boolean setupEconomy() {
		if (this.instance.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = this.instance.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
}
