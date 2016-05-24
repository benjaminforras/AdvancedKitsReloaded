package hu.tryharddood.advancedkits.Listeners;

import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void OnPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        KitManager.getKits().stream().filter(Kit::isFirstjoin).forEachOrdered(kit -> {
            if (!KitManager.getFirstJoin(player, kit)) {
                Bukkit.dispatchCommand(player, "kit use " + kit.getName());
                KitManager.setFirstJoin(player, kit);
            }
        });
    }
}
