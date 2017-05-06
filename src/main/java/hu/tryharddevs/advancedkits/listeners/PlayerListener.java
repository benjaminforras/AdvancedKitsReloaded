package hu.tryharddevs.advancedkits.listeners;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.Config;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.FIRSTJOIN;
import static hu.tryharddevs.advancedkits.utils.MessagesApi.sendMessage;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public class PlayerListener implements Listener {
	private AdvancedKitsMain instance;

	public PlayerListener(AdvancedKitsMain instance) {
		this.instance = instance;
	}


	@EventHandler
	public void onPlayerJoin(PlayerLoginEvent event) {
		final Player player = event.getPlayer();
		this.instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> KitManager.getKits().stream().filter(kit -> kit.getFlag(FIRSTJOIN, player.getWorld().getName())).forEach(kit -> {
			Bukkit.dispatchCommand(player, "kit use " + kit.getName());
		}), 2L);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		User.getUser(event.getPlayer().getUniqueId()).save();
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		ItemStack itemStack = event.getItemInHand();
		if (Objects.isNull(itemStack) || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) return;

		final Block block = event.getBlock();
		if (block.getType() == Material.CHEST) {
			Kit kit = KitManager.getKit(itemStack.getItemMeta().getDisplayName(), event.getPlayer().getWorld().getName());
			if (Objects.nonNull(kit)) {
				if (Config.DISABLED_WORLDS.contains(event.getPlayer().getWorld().getName())) {
					sendMessage(event.getPlayer(), getMessage("kitUseDisabledInWorld"));
					event.setCancelled(true);
					return;
				}

				ArrayList<ItemStack> items = new ArrayList<>(kit.getItems());
				items.addAll(kit.getArmors());

				if ((items.size()) > 27) {
					Block northBlock = block.getRelative(BlockFace.NORTH);
					Block southBlock = block.getRelative(BlockFace.SOUTH);
					Block westBlock  = block.getRelative(BlockFace.WEST);
					Block eastBlock  = block.getRelative(BlockFace.EAST);
					if (Objects.isNull(northBlock) || northBlock.getType().equals(Material.AIR)) {
						northBlock.setType(Material.CHEST);
					} else if (Objects.isNull(southBlock) || northBlock.getType().equals(Material.AIR)) {
						southBlock.setType(Material.CHEST);

					} else if (Objects.isNull(westBlock) || northBlock.getType().equals(Material.AIR)) {
						westBlock.setType(Material.CHEST);

					} else if (Objects.isNull(eastBlock) || northBlock.getType().equals(Material.AIR)) {
						eastBlock.setType(Material.CHEST);
					} else {
						event.getPlayer().sendMessage(Config.CHAT_PREFIX + " " + getMessage("cantPlaceChest"));
						event.setCancelled(true);
						return;
					}
				}

				if (block.getState() instanceof DoubleChest) {
					DoubleChest chest    = (DoubleChest) block.getState();
					Inventory   chestinv = chest.getInventory();

					chestinv.addItem(items.toArray(new ItemStack[items.size()]));
				} else if (block.getState() instanceof Chest) {
					Chest     chest    = (Chest) block.getState();
					Inventory chestinv = chest.getInventory();

					chestinv.addItem(items.toArray(new ItemStack[items.size()]));
				}
			}
		}
	}

	@EventHandler
	public void onPlayerClickSignEvent(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getState() instanceof Sign) {
				Player player = event.getPlayer();
				Sign   sign   = (Sign) event.getClickedBlock().getState();

				if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[" + ChatColor.DARK_BLUE + "Kits" + ChatColor.GRAY + "]")) {
					Kit kit = KitManager.getKit(ChatColor.stripColor(sign.getLine(1)), player.getWorld().getName());
					if (kit == null) {
						this.instance.log(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + sign.getLocation().toString());
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
	public void onPlayerCreateSignEvent(SignChangeEvent event) {
		if (event.getLine(0) == null) {
			return;
		}
		Player player = event.getPlayer();

		if (!player.hasPermission("advancedkits.create")) {
			return;
		}

		if (Arrays.asList("kit|akit|advancedkits|kits|akits", "[kit]", "[kits]", "kit|akit|advancedkits|kits|akits", "[Kit]", "[Kits]").contains(event.getLine(0))) {
			event.setLine(0, ChatColor.GRAY + "[" + ChatColor.DARK_BLUE + "Kits" + ChatColor.GRAY + "]");
			if (event.getLine(1) == null) {
				this.instance.log(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + event.getBlock().getLocation().toString());
				player.sendMessage(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + event.getBlock().getLocation().toString());
				return;
			}

			Kit kit = KitManager.getKit(ChatColor.stripColor(event.getLine(1)), player.getWorld().getName());
			if (kit == null) {
				this.instance.log(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + event.getBlock().getLocation().toString());
				player.sendMessage(ChatColor.RED + "Error: Kit doesn't exists. Sign location: " + event.getBlock().getLocation().toString());
				return;
			}

			event.setLine(1, ChatColor.GREEN + event.getLine(1));
		}
	}
}
