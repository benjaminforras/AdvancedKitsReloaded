package hu.tryharddevs.advancedkits.utils.invapi.listeners;


import hu.tryharddevs.advancedkits.utils.invapi.PageInventory;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface PagesItemClickListener
{

	/**
	 * Called when a player clicks a page
	 *
	 * @param event {@link InventoryClickEvent}
	 */
	void interact(PageInventory pageInventory, InventoryClickEvent event);

}
