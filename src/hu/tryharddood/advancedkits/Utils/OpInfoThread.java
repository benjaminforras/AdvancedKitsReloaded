package hu.tryharddood.advancedkits.Utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class OpInfoThread extends Thread
{

    private final Player player;

    public OpInfoThread(Player p)
    {
        this.player = p;
    }

    @Override
    public void run()
    {
        this.player.sendMessage(ChatColor.GOLD + "[AdvancedKitsReloaded]");
        this.player.sendMessage(ChatColor.GRAY + "");
        this.player.sendMessage(ChatColor.GOLD + "Current Version: " + ChatColor.GREEN + UpdateManager.CURRENT_VERSION);
        this.player.sendMessage(ChatColor.GOLD + "Latest Version:   " + ChatColor.GREEN + UpdateManager.LATEST_VERSION);
    }

}
