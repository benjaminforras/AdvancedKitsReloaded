package hu.tryharddevs.advancedkits.utils.invapi.events;

import hu.tryharddevs.advancedkits.utils.invapi.ClickInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ItemClickEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	protected int                 slot;
	private   boolean             cancelled;
	private   InventoryClickEvent invEvent;

	public ItemClickEvent(int slot, InventoryClickEvent invEvent)
	{
		this.slot = slot;
		this.invEvent = invEvent;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	public InventoryClickEvent getEvent()
	{
		return invEvent;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public abstract ClickInventory getInventory();

	public abstract ItemStack getItemStack();

	public String getName()
	{
		return getInventory().getName();
	}

	public abstract Player getPlayer();

	public int getSlot()
	{
		return slot;
	}

	public boolean isCancelled()
	{
		return cancelled;
	}

	public void setCancelled(boolean cancel)
	{
		cancelled = cancel;
	}

}
