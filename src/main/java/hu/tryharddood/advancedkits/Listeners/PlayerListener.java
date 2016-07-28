package hu.tryharddood.advancedkits.Listeners;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.SubCommands.UseCommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Map;

/**
 * Class: PlayerListener
 *
 * @author TryHardDood
 */
public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerLoginEvent event) {
		final Player player = event.getPlayer();
		AdvancedKits.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(AdvancedKits.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Map.Entry<String, Kit> kit : AdvancedKits.getKitManager().getKits().entrySet())
				{
					if (kit.getValue().isFirstjoin())
					{
						if (!AdvancedKits.getKitManager().getFirstJoin(player, kit.getValue()))
						{
							UseCommand.GiveItems(player, kit.getValue());
							AdvancedKits.getKitManager().setFirstJoin(player, kit.getValue());
						}
					}
				}
			}
		}, 2L);
	}
}
