package hu.tryharddevs.advancedkits.utils.invapi.listeners;

import hu.tryharddevs.advancedkits.utils.invapi.NamedInventory;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface NamedPageChangeListener
{
	/**
	 * Called when a player changes a page
	 *
	 * @param event {@link InventoryClickEvent}
	 */
	void interact(NamedInventory namedInventory, InventoryClickEvent event);
}
