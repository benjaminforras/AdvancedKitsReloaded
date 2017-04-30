package hu.tryharddevs.advancedkits.utils.invapi.listeners;

import org.bukkit.event.inventory.InventoryCloseEvent;

public interface InventoryCloseListener
{
	/**
	 * Called when a player closes the inventory
	 *
	 * @param event {@link InventoryCloseEvent}
	 */
	void interact(InventoryCloseEvent event);
}
