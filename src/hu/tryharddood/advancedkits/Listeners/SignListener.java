package hu.tryharddood.advancedkits.Listeners;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class SignListener implements Listener
{
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Action action = event.getAction();

		if (action == Action.RIGHT_CLICK_BLOCK)
		{
			if (event.getClickedBlock().getState() instanceof Sign)
			{
				Player player = event.getPlayer();
				Sign   sign   = (Sign) event.getClickedBlock().getState();

				if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[" + ChatColor.DARK_BLUE + "Kits" + ChatColor.GRAY + "]"))
				{
					Kit kit = AdvancedKits.getKitManager().getKit(ChatColor.stripColor(sign.getLine(1)));
					if (kit == null)
					{
						AdvancedKits.log(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + sign.getLocation().toString());
						event.setCancelled(true);
						return;
					}

					Bukkit.dispatchCommand(player, "kit use " + kit.getName());
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void SignChangeEvent(SignChangeEvent sign)
	{
		if (sign.getLine(0) == null)
		{
			return;
		}
		Player player = sign.getPlayer();

		if (Arrays.asList("kit", "[kit]", "[kits]", "Kit", "[Kit]", "[Kits]").contains(sign.getLine(0)) && player.hasPermission(Variables.KITADMIN_PERMISSION))
		{
			sign.setLine(0, ChatColor.GRAY + "[" + ChatColor.DARK_BLUE + "Kits" + ChatColor.GRAY + "]");
			if (sign.getLine(1) == null)
			{
				AdvancedKits.log(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + sign.getBlock().getLocation().toString());
				player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + sign.getBlock().getLocation().toString());
				return;
			}

			Kit kit = AdvancedKits.getKitManager().getKit(ChatColor.stripColor(sign.getLine(1)));
			if (kit == null)
			{
				AdvancedKits.log(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + sign.getBlock().getLocation().toString());
				player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + sign.getBlock().getLocation().toString());
				return;
			}

			sign.setLine(1, ChatColor.GREEN + sign.getLine(1));
		}
	}
}
