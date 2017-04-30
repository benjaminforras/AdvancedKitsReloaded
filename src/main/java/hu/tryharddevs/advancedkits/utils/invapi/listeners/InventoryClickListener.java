package hu.tryharddevs.advancedkits.utils.invapi.listeners;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface InventoryClickListener
{
	/**
	 * Called when a player clicks the inventory
	 *
	 * @param event {@link InventoryClickEvent}
	 */
	void interact(InventoryClickEvent event);
}
