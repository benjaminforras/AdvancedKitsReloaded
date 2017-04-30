package hu.tryharddevs.advancedkits.utils.invapi.listeners;

import hu.tryharddevs.advancedkits.utils.invapi.NamedInventory;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface NamedPageClickListener
{
	/**
	 * Called when a player closes a page
	 *
	 * @param event {@link InventoryClickEvent}
	 */
	void interact(NamedInventory namedInventory, InventoryClickEvent event);
}
