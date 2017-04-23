package hu.tryharddevs.advancedkits.listeners;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.FIRSTJOIN;

public class PlayerListener implements Listener
{
	private AdvancedKitsMain instance = AdvancedKitsMain.advancedKits;


	@EventHandler
	public void onPlayerJoin(PlayerLoginEvent event)
	{
		//TODO: Better firstjoin
		final Player player = event.getPlayer();
		instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> KitManager.getKits().stream().filter(kit -> kit.getFlag(FIRSTJOIN, player.getWorld().getName())).forEach(kit -> {
			Bukkit.dispatchCommand(player, "kit use " + kit.getName());
		}), 2L);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		User.getUser(event.getPlayer().getUniqueId()).save();
	}

	@EventHandler
	public void onPlayerClickSignEvent(PlayerInteractEvent event)
	{
		Action action = event.getAction();
		if (action == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getState() instanceof Sign) {
				Player player = event.getPlayer();
				Sign   sign   = (Sign) event.getClickedBlock().getState();

				if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[" + ChatColor.DARK_BLUE + "Kits" + ChatColor.GRAY + "]")) {
					Kit kit = KitManager.getKit(ChatColor.stripColor(sign.getLine(1)), player.getWorld().getName());
					if (kit == null) {
						instance.log(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + sign.getLocation().toString());
						event.setCancelled(true);
						return;
					}

					Bukkit.dispatchCommand(player, "akit use " + kit.getName());
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerCreateSignEvent(SignChangeEvent event)
	{
		if (event.getLine(0) == null) {
			return;
		}
		Player player = event.getPlayer();

		if (!player.hasPermission("advancedkits.create")) {
			return;
		}

		if (Arrays.asList("kit", "[kit]", "[kits]", "Kit", "[Kit]", "[Kits]").contains(event.getLine(0))) {
			event.setLine(0, ChatColor.GRAY + "[" + ChatColor.DARK_BLUE + "Kits" + ChatColor.GRAY + "]");
			if (event.getLine(1) == null) {
				instance.log(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + event.getBlock().getLocation().toString());
				player.sendMessage(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + event.getBlock().getLocation().toString());
				return;
			}

			Kit kit = KitManager.getKit(ChatColor.stripColor(event.getLine(1)), player.getWorld().getName());
			if (kit == null) {
				instance.log(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + event.getBlock().getLocation().toString());
				player.sendMessage(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + event.getBlock().getLocation().toString());
				return;
			}

			event.setLine(1, ChatColor.GREEN + event.getLine(1));
		}
	}
}
