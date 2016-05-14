package hu.tryharddood.advancedkits.Utils.TitleAPI;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TitleSendEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private String title;
    private String subtitle;
    private boolean cancelled = false;

    public TitleSendEvent(Player player, String title, String subtitle)
    {
        this.player = player;
        this.title = title;
        this.subtitle = subtitle;
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

    public Player getPlayer()
    {
        return player;
    }

    public String getSubtitle()
    {
        return subtitle;
    }

    public void setSubtitle(String subtitle)
    {
        this.subtitle = subtitle;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

}
