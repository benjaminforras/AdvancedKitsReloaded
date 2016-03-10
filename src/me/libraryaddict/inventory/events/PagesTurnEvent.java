package me.libraryaddict.inventory.events;

import me.libraryaddict.inventory.PageInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PagesTurnEvent extends ItemClickEvent
{
    private static final HandlerList handlers = new HandlerList();
    private final PageInventory inv;
    private final int newPage;

    public PagesTurnEvent(PageInventory inventory, int slot, InventoryClickEvent invEvent, int newPage)
    {
        super(slot, invEvent);
        this.inv = inventory;
        this.newPage = newPage;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public int getNewPage()
    {
        return newPage;
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public PageInventory getInventory()
    {
        return inv;
    }

    public ItemStack getItemStack()
    {
        if (slot >= 0) return inv.getItem(slot);
        return null;
    }

    @Override
    public Player getPlayer()
    {
        return inv.getPlayer();
    }
}
