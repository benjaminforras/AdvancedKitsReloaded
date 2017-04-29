package hu.tryharddevs.advancedkits.utils.menuapi.core;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.utils.menuapi.libraries.MenuRegistry;
import hu.tryharddevs.advancedkits.utils.menuapi.listeners.MenuActions;
import hu.tryharddevs.advancedkits.utils.menuapi.listeners.MenuCloseAction;

/* NOTE: The following applies to all content within this entire Jar.
 * These files are FREE and licensed under the GPL. If using my code,
 * please recognize:
 * - All files herein are under no warranty.
 * - You may not attempt to make money off of my work.
 * - If using over 60 lines of my code, please provide visible credit.
 */

public class MenuAPI {
	private static MenuRegistry menuRegistry;
	private static MenuAPI      plugin;
	private AdvancedKitsMain instance = AdvancedKitsMain.getPlugin();

	public static MenuAPI i() {
		return plugin;
	}

	public void onEnable() {
		plugin = this;
		menuRegistry = new MenuRegistry();

		instance.getServer().getPluginManager().registerEvents(new MenuActions(), instance);
		instance.getServer().getPluginManager().registerEvents(new MenuCloseAction(), instance);
	}

	public MenuRegistry getMenuRegistry() {
		return menuRegistry;
	}
}