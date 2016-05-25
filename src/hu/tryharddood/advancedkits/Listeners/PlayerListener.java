package hu.tryharddood.advancedkits.Listeners;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.SubCommands.UseCommand;
import hu.tryharddood.advancedkits.Kits.KitManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Class: PlayerListener
 *
 * @author TryHardDood
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerLoginEvent event) {
        AdvancedKits.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(AdvancedKits.getInstance(), () -> {
            Player player = event.getPlayer();

            KitManager.getKits().stream().forEach(kit ->
            {
                if(!KitManager.getFirstJoin(player, kit))
                {
                    UseCommand.GiveItems(player, kit);
                    KitManager.setFirstJoin(player, kit);
                }
            });
        }, 2L);
    }
}
