package hu.tryharddevs.advancedkits.utils.invapi.listeners;


import org.bukkit.event.inventory.InventoryClickEvent;

public interface PagesTurnListener
{
	/**
	 * Called when a player turns page
	 *
	 * @param event {@link InventoryClickEvent}
	 */
	void interact(InventoryClickEvent event);
}
