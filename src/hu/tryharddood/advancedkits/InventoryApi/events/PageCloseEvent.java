package hu.tryharddood.advancedkits.InventoryApi.events;

import hu.tryharddood.advancedkits.InventoryApi.PageInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PageCloseEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final PageInventory inv;

    public PageCloseEvent(PageInventory inventory)
    {
        this.inv = inventory;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public PageInventory getInventory()
    {
        return inv;
    }

    public String getName()
    {
        return getInventory().getName();
    }

    public Player getPlayer()
    {
        return inv.getPlayer();
    }

}
