package hu.tryharddevs.advancedkits.cinventory.listeners;

import org.bukkit.event.inventory.InventoryDragEvent;

public interface InventoryDragListener
{
	/**
	 * Called when a player closes the inventory
	 *
	 * @param event {@link InventoryDragEvent}
	 */
	void interact(InventoryDragEvent event);
}
