package hu.tryharddood.advancedkits.InventoryApi.events;

import hu.tryharddood.advancedkits.InventoryApi.PageInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PagesTurnEvent extends ItemClickEvent {
	private static final HandlerList handlers = new HandlerList();
	private final PageInventory inv;
	private final int           newPage;

	public PagesTurnEvent(PageInventory inventory, int slot, InventoryClickEvent invEvent, int newPage) {
		super(slot, invEvent);
		this.inv = inventory;
		this.newPage = newPage;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public PageInventory getInventory() {
		return inv;
	}

	@Override
	public ItemStack getItemStack() {
		if (slot >= 0)
		{
			return inv.getItem(slot);
		}
		return null;
	}

	public int getNewPage() {
		return newPage;
	}

	@Override
	public Player getPlayer() {
		return inv.getPlayer();
	}
}
