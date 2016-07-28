package hu.tryharddood.advancedkits.InventoryApi.events;

import hu.tryharddood.advancedkits.InventoryApi.PageInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PagesClickEvent extends ItemClickEvent {
	private static final HandlerList handlers = new HandlerList();
	private final PageInventory inv;

	public PagesClickEvent(PageInventory inventory, int slot, InventoryClickEvent invEvent) {
		super(slot, invEvent);
		this.inv = inventory;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
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

	@Override
	public Player getPlayer() {
		return inv.getPlayer();
	}
}
