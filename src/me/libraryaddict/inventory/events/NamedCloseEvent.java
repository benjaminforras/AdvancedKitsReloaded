package me.libraryaddict.inventory.events;

import me.libraryaddict.inventory.NamedInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NamedCloseEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final NamedInventory inv;

    public NamedCloseEvent(NamedInventory inventory)
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

    public NamedInventory getInventory()
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
