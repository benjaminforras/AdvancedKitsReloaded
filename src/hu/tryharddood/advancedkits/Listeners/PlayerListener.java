package hu.tryharddood.advancedkits.Listeners;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.SubCommands.UseCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Class: PlayerListener
 *
 * @author TryHardDood
 */
public class PlayerListener implements Listener
{

    @EventHandler
    public void onPlayerJoin(PlayerLoginEvent event)
    {
        AdvancedKits.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(AdvancedKits.getInstance(), () -> {
            Player player = event.getPlayer();

            AdvancedKits.getKitManager().getKits().stream().forEach(kit ->
                                                                    {
                                                                        if (!AdvancedKits.getKitManager().getFirstJoin(player, kit))
                                                                        {
                                                                            UseCommand.GiveItems(player, kit);
                                                                            AdvancedKits.getKitManager().setFirstJoin(player, kit);
                                                                        }
                                                                    });
        }, 2L);
    }
}
