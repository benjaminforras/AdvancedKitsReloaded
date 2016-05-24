package hu.tryharddood.advancedkits.Listeners;

import fr.xephi.authme.events.LoginEvent;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class PlayerListener_AuthMe implements Listener {

    @EventHandler
    public void OnPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();
        KitManager.getKits().stream().filter(Kit::isFirstjoin).forEachOrdered(kit -> {
            if (!KitManager.getFirstJoin(player, kit)) {
                Bukkit.dispatchCommand(player, "kit use " + kit.getName());
                KitManager.setFirstJoin(player, kit);
            }
        });
    }
}
