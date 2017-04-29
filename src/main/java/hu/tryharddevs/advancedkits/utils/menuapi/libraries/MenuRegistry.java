package hu.tryharddevs.advancedkits.utils.menuapi.libraries;

import hu.tryharddevs.advancedkits.utils.menuapi.components.Menu;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ColonelHedgehog on 12/12/14.
 * You have freedom to modify given sources. Please credit me as original author.
 * Keep in mind that this is not for sale.
 */

public class MenuRegistry {
	private List<Menu> menus;

	public MenuRegistry() {
		menus = new ArrayList<>();
	}

	public List<Menu> getMenus() {
		return menus;
	}

	public void register(Menu inv) {
		menus.add(inv);
	}

	public void deregister(Menu inv) {
		menus.remove(inv);
	}

	public Menu getByInventory(Inventory inventory) {
		for (Menu menu : menus) {
			if (menu.getInventory().equals(inventory)) {
				return menu;
			}
		}

		return null;
	}
}
