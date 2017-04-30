package hu.tryharddevs.advancedkits.utils.invapi.listeners;

import hu.tryharddevs.advancedkits.utils.invapi.PageInventory;
import org.bukkit.entity.Player;

public interface PageCloseListener
{
	/**
	 * Called when a player closes a page
	 *
	 * @param player        player
	 * @param pageInventory PageInventory
	 */
	void interact(Player player, PageInventory pageInventory);
}
