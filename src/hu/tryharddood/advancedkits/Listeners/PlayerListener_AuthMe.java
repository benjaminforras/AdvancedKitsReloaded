package hu.tryharddood.advancedkits.Listeners;

import fr.xephi.authme.events.LoginEvent;
import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.SubCommands.UseCommand;
import hu.tryharddood.advancedkits.Kits.KitManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class PlayerListener_AuthMe implements Listener
{

    @EventHandler
    public void OnPlayerLogin(LoginEvent event)
    {
        Player player = event.getPlayer();
        AdvancedKits.log("Teszt11");

        KitManager.getKits().stream().filter(kit -> !KitManager.getFirstJoin(player, kit)).forEach(kit -> {
            AdvancedKits.log("Teszt2");
            if (!KitManager.getFirstJoin(player, kit))
            {
                AdvancedKits.log("Teszt3");
                UseCommand.GiveItems(player, kit);
                KitManager.setFirstJoin(player, kit);
            }
        });
    }
}
