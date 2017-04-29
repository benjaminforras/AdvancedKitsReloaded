package hu.tryharddevs.advancedkits.utils.menuapi.listeners;

import hu.tryharddevs.advancedkits.utils.menuapi.components.Menu;
import hu.tryharddevs.advancedkits.utils.menuapi.core.MenuAPI;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

/**
 * Created by ColonelHedgehog on 1/26/15.
 * You have freedom to modify given sources. Please credit me as original author.
 * Keep in mind that this is not for sale.
 */
public class MenuCloseAction implements Listener {
	@EventHandler public void onClose(InventoryCloseEvent event) {
		Menu menu = MenuAPI.i().getMenuRegistry().getByInventory(event.getInventory());

		List<HumanEntity> viewers = event.getViewers();
		if (menu != null) {
			viewers.remove(event.getPlayer()); // Precaution, really.
			if (viewers.size() == 0) {
				menu.getObjects().clear();
				menu.getInventory().clear();
				MenuAPI.i().getMenuRegistry().deregister(menu);
			}
		}
	}
}
