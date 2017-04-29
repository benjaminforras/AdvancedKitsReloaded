package hu.tryharddevs.advancedkits.utils.menuapi.listeners;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.utils.menuapi.components.Coordinates;
import hu.tryharddevs.advancedkits.utils.menuapi.components.Menu;
import hu.tryharddevs.advancedkits.utils.menuapi.components.MenuObject;
import hu.tryharddevs.advancedkits.utils.menuapi.components.sub.GUISound;
import hu.tryharddevs.advancedkits.utils.menuapi.core.MenuAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by ColonelHedgehog on 12/11/14.
 * You have freedom to modify given sources. Please credit me as original author.
 * Keep in mind that this is not for sale.
 */
public class MenuActions implements Listener {
	@EventHandler public void onClick(final InventoryClickEvent event) {
		if (event.getCurrentItem() == null) {
			return;
		}

		final Menu menu = MenuAPI.i().getMenuRegistry().getByInventory(event.getInventory());
		if (menu == null) {
			return;
		}

		event.setCancelled(true);

		final MenuObject menuObject = menu.getItemAt(new Coordinates(menu, event.getSlot()));

		if (menuObject == null) {
			return;
		}


		if (menuObject.getActionListener() == null) {
			return;
		}

		new BukkitRunnable() {
			@Override public void run() {
				GUISound sound = menuObject.getGUISound();
				if (menuObject.getGUISound() != null) {
					if (sound.getPlayOnClick()) {
						sound.playGUISound((Player) event.getWhoClicked());
					}
				}
				menuObject.getActionListener().onClick(event.getClick(), menuObject, (Player) event.getWhoClicked());
			}
		}.runTask(AdvancedKitsMain.getPlugin());
	}
}
