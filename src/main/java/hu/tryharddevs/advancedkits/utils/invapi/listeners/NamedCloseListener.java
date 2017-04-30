package hu.tryharddevs.advancedkits.utils.invapi.listeners;

import hu.tryharddevs.advancedkits.utils.invapi.NamedInventory;
import org.bukkit.entity.Player;

public interface NamedCloseListener
{
	/**
	 * Called when a player closes NamedInventory
	 *
	 * @param player         Player
	 * @param namedInventory NamedInventory
	 */
	void interact(Player player, NamedInventory namedInventory);
}
