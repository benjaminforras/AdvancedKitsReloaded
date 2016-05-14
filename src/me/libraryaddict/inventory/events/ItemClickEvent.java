package me.libraryaddict.inventory.events;

import me.libraryaddict.inventory.ClickInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ItemClickEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    protected final int slot;
    private final InventoryClickEvent invEvent;
    private boolean cancelled;

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

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    protected abstract ClickInventory<?> getInventory();

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

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        cancelled = cancel;
    }

}
