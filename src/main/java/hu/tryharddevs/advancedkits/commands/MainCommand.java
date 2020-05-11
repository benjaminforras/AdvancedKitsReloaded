package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.contexts.OnlinePlayer;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.Config;
import hu.tryharddevs.advancedkits.cinventory.inventories.CPageInventory;
import hu.tryharddevs.advancedkits.cinventory.inventories.CSimpleInventory;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.kits.flags.Flag;
import hu.tryharddevs.advancedkits.kits.flags.InvalidFlagValueException;
import hu.tryharddevs.advancedkits.utils.ItemBuilder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.utils.MessagesApi.sendMessage;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@CommandAlias("%rootcommand")
public class MainCommand extends BaseCommand {
	private final AdvancedKitsMain instance;

	public MainCommand(AdvancedKitsMain instance) {
		this.instance = instance;
	}


	@Default
	public void onDefault(CommandSender sender) {
		Player player = sender instanceof Player ? (Player) sender : null;
		if (Objects.isNull(player)) onHelp(sender);
		else Bukkit.dispatchCommand(player, "kit view");
	}

	@Subcommand("reload")
	@CommandPermission("advancedkits.reload")
	public void onReloadCommand(CommandSender sender) {
		sendMessage(sender, "Starting to reload configuration.");
		Config.loadConfigurationValues(instance);
		sendMessage(sender, "Done reloading the configuration.");

		sendMessage(sender, "Loading KitManager.");
		instance.getKitManager().loadKits();
		sendMessage(sender, "Done loading KitManager.");
	}

	@Subcommand("help")
	public void onHelp(CommandSender sender) {
		ArrayList<String> helpList = new ArrayList<>();
		sender.sendMessage(ChatColor.YELLOW + "--------- " + ChatColor.WHITE + Config.CHAT_PREFIX + " Help " + ChatColor.YELLOW + " ---------------------");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "use <kitname>" + ": " + ChatColor.WHITE + "Uses the free or bought kit.");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "view <kitname>" + ": " + ChatColor.WHITE + "Views the kit items and armor.");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "create <kitname>" + ": " + ChatColor.WHITE + "Creates the kit with the items and armor in your inventory");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "delete <kitname>" + ": " + ChatColor.WHITE + "Deletes the kit");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "edit <kitname>" + ": " + ChatColor.WHITE + "Edits the kit.");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "flag <kitname> <flag> <value> [world]" + ": " + ChatColor.WHITE + "Sets a flag.");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "give <kitname> <player> [forceuse]" + ": " + ChatColor.WHITE + "Gives the kit to the player.");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "reload" + ": " + ChatColor.WHITE + "Reloads the kits and the configuration");

		sender.sendMessage(helpList.toArray(new String[helpList.size()]));
	}

	@Subcommand("delete")
	@CommandPermission("advancedkits.delete")
	@Syntax("<kitname>")
	@CommandCompletion("@kits")
	public void onDeleteCommand(CommandSender sender, @Optional Kit kit) {

		Player player = sender instanceof Player ? (Player) sender : null;

		if (Objects.isNull(kit) && Objects.nonNull(player)) {
			String world = player.getWorld().getName();

			CPageInventory cPageInventory = new CPageInventory("AdvancedKits - View Kit", (Player) sender);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world)).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world).clone()).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).hideAttributes().toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			cPageInventory.openInventory();

			cPageInventory.onInventoryClickEvent((_event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;
				if (Arrays.asList(cPageInventory.getBackPage(), cPageInventory.getForwardsPage()).contains(clickedItem))
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_player.closeInventory();
				Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit delete " + clickedKit.getName());
			});
			return;
		} else if (Objects.isNull(kit) && Objects.isNull(player)) {
			sendMessage(sender, "Syntax: /kit delete <kitname>");
			return;
		}

		String name = kit.getName();
		if (Objects.nonNull(player)) {

			CSimpleInventory cSimpleInventory = new CSimpleInventory("AdvancedKits - Delete Kit", player);

			cSimpleInventory.setItem(2, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setName(getMessage("guiConfirm")).toItemStack());
			cSimpleInventory.setItem(6, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setName(getMessage("guiCancel")).toItemStack());
			cSimpleInventory.openInventory();

			cSimpleInventory.onInventoryClickEvent((_event) -> {
				if (_event.getCurrentItem() == null) return;

				Material item = _event.getCurrentItem().getType();
				if (item == Material.RED_STAINED_GLASS_PANE) //Cancel
				{
					_event.getWhoClicked().closeInventory();
				} else if (item == Material.GREEN_STAINED_GLASS_PANE) //Delete
				{
					_event.getWhoClicked().closeInventory();
					instance.getKitManager().deleteKit(kit);
					sendMessage(player, getMessage("successfullyDeleted", name));
				}
			});
		} else {
			instance.getKitManager().deleteKit(kit);
			sendMessage(sender, getMessage("successfullyDeleted", name));
		}
	}

	@Subcommand("buy")
	@CommandPermission("advancedkits.buy")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onBuyCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();

		if (Config.DISABLED_WORLDS.contains(world)) {
			sendMessage(player, getMessage("kitUseDisabledInWorld"));
			return;
		}

		if (Objects.isNull(kit)) {

			CPageInventory cPageInventory = new CPageInventory("AdvancedKits - Buy Kit", player);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world) && (!_kit.getFlag(FREE, world) && !user.isUnlocked(_kit))).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world).clone()).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).hideAttributes().toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			cPageInventory.openInventory();

			cPageInventory.onInventoryClickEvent((_event) -> {

				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
					return;
				}
				if (Arrays.asList(cPageInventory.getBackPage(), cPageInventory.getForwardsPage()).contains(clickedItem))
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_player.closeInventory();
				Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit buy " + clickedKit.getName());
			});
			return;
		}

		if (!player.hasPermission(kit.getPermission())) {
			sendMessage(player, getMessage("noKitPermission"));
			return;
		}


		if (user.isUnlocked(kit)) {
			sendMessage(player, getMessage("alreadyUnlocked", kit.getName()));
			return;
		}

		EconomyResponse r = instance.getEconomy().withdrawPlayer(player, kit.getFlag(COST, world));
		if (r.transactionSuccess()) {
			sendMessage(player, getMessage("successfullyBought", kit.getDisplayName(world)));

			user.addToUnlocked(kit);
			user.save();

			if (kit.getFlag(USEONBUY, world))
				Bukkit.dispatchCommand(player, "advancedkitsreloaded:kit use " + kit.getName());
		} else {
			sendMessage(player, getMessage("notEnoughMoney", r.amount));
		}
	}

	@Subcommand("give")
	@CommandPermission("advancedkits.give")
	@CommandCompletion("@kits @players true|false true|false")
	@Syntax("<kitname> <player> [forceuse, default=false] [unlock, default=false]")
	public void onGiveCommand(CommandSender sender, Kit kit, OnlinePlayer player, @Default("false") Boolean forceUse, @Default("false") Boolean unlockKit) {

		if (Objects.isNull(player)) {
			sendMessage(sender, getMessage("playerNotFound"));
			return;
		}
		if (player.getPlayer().isDead()) {
			sendMessage(sender, getMessage("playerIsDead"));
			return;
		}

		User user = User.getUser(player.getPlayer().getUniqueId());

		sender.sendMessage(forceUse + " " + unlockKit);

		// /kit give kit name true
		if (forceUse) {
			if (!user.isUnlocked(kit) && unlockKit) { // /kit give kit name true true
				user.addToUnlocked(kit);
				user.save();
			}
			UseCommand.giveKitToPlayer(player.getPlayer(), kit);
			sendMessage(sender, getMessage("successfullyGiven", kit.getName(), player.getPlayer().getName()));
		} else if (!user.isUnlocked(kit) && unlockKit) { // /kit give kit name false true
			user.addToUnlocked(kit);
			user.save();
			sendMessage(sender, getMessage("successfullyGiven", kit.getName(), player.getPlayer().getName()));
		} else {
			sendMessage(sender, getMessage("giveAlreadyUnlocked", kit.getName()));
		}
	}

	@Subcommand("view")
	@CommandPermission("advancedkits.view")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onViewCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();

		if (Config.DISABLED_WORLDS.contains(world)) {
			sendMessage(player, getMessage("kitUseDisabledInWorld"));
			return;
		}

		if (Objects.isNull(kit)) {
			CPageInventory cPageInventory = new CPageInventory("Kit Auswahl", player);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world))
			        .sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world).clone())
			                .setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).hideAttributes()
			                .toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			cPageInventory.openInventory();

			cPageInventory.onInventoryClickEvent((_event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;
				if (Arrays.asList(cPageInventory.getBackPage(), cPageInventory.getForwardsPage()).contains(clickedItem))
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					return;
				}

				_player.closeInventory();
				if(_event.getClick().isRightClick()) {
                    Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit view " + clickedKit.getName());
                } else {
                    Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit use " + clickedKit.getName());
                    if(clickedKit.getFlag(KEEPINVENTORYOPEN, world)) {
                        Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit view");
                    }
                }
			});
			return;
		}

		CSimpleInventory cSimpleInventory = new CSimpleInventory("Kit-Vorschau", player, 54);
		cSimpleInventory.addItems(kit.getItems());

		int i = 36;
		for (ItemStack armor : kit.getArmors()) {
			cSimpleInventory.setItem(i, armor);
			i++;
		}

		cSimpleInventory.setItem(49, new ItemBuilder(Material.PAPER).setName(getMessage("informations")).setLore(KitManager.getKitDescription(player, kit, world)).toItemStack());

		if (user.isUnlocked(kit) || kit.getFlag(FREE, world)) {
			cSimpleInventory.setItem(53, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setName(ChatColor.GREEN + getMessage("guiUse")).hideAttributes().toItemStack());
		} else if (kit.getFlag(COST, world) > 0) {
			cSimpleInventory.setItem(53, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setName(ChatColor.GREEN + getMessage("guiBuy")).hideAttributes().toItemStack());
		}

		cSimpleInventory.setItem(45, new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setName(ChatColor.GREEN + getMessage("guiBackToMenu")).hideAttributes().toItemStack());
		cSimpleInventory.openInventory();

		cSimpleInventory.onInventoryClickEvent((_event) -> {
			ItemStack clickedItem = _event.getCurrentItem();
			if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
				return;

			Player _player = (Player) _event.getWhoClicked();
			if (clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE || clickedItem.getType() == Material.RED_STAINED_GLASS_PANE|| clickedItem.getType() == Material.WHITE_STAINED_GLASS_PANE) {
				if (clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
					Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit use " + kit.getName());
					if(kit.getFlag(KEEPINVENTORYOPEN, world)) {
                        Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit view");
                    }
				} else if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
					Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit buy " + kit.getName());
				} else if (clickedItem.getType() == Material.WHITE_STAINED_GLASS_PANE) {
					Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit view");
				}
			}
		});
		sendMessage(player, getMessage("kitView", kit.getDisplayName(world)));
	}

	@Subcommand("flag")
	@CommandPermission("advancedkits.flag")
	@CommandCompletion("@kits @flags")
	@Syntax("<kitname> <flag> <value> [world]")
	public <T> void onFlagCommand(CommandSender sender, Kit kit, Flag<T> flag, String value, @Optional String world) {

		String   tempValue     = String.join(" ", value, Objects.isNull(world) ? "" : world);
		String[] splittedValue = tempValue.split(" ");

		if (Objects.nonNull(Bukkit.getWorld(splittedValue[splittedValue.length - 1]))) {
			world = splittedValue[splittedValue.length - 1];
			value = String.join(" ", Arrays.copyOf(splittedValue, splittedValue.length - 1));
		} else {
			world = "global";
			value = String.join(" ", splittedValue);
		}

		if (value.equalsIgnoreCase("hand")) {
			Player player = sender instanceof Player ? (Player) sender : null;
			if (Objects.isNull(player)) {
				sendMessage(sender, getMessage("playerOnly"));
				return;
			}

			if (flag.getName().equalsIgnoreCase("firework")) {
				if (Objects.isNull(player.getInventory().getItemInMainHand()) || !player.getInventory().getItemInMainHand().getType().equals(Material.FIREWORK_ROCKET)) {
					sendMessage(player, getMessage("notFirework"));
					return;
				}

				try {
					kit.setFlag(flag, world, flag.parseItem(player));
				} catch (InvalidFlagValueException e) {
					player.sendMessage(e.getMessages());
					return;
				}

				sendMessage(player, getMessage("flagSet", flag.getName(), value, kit.getDisplayName(world), world));
				return;
			} else if (flag.getName().equalsIgnoreCase("icon")) {
				if (Objects.isNull(player.getInventory().getItemInMainHand()) || player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
					sendMessage(player, getMessage("notValidIcon"));
					return;
				}
				try {
					kit.setFlag(flag, world, flag.parseItem(player));
				} catch (InvalidFlagValueException e) {
					player.sendMessage(e.getMessages());
					return;
				}

				sendMessage(player, getMessage("flagSet", flag.getName(), value, kit.getDisplayName(world), world));
				return;
			}
		}

		if (flag.getName().equalsIgnoreCase("firework") || flag.getName().equalsIgnoreCase("icon")) {
			sendMessage(sender, ChatColor.GRAY + "Usage: /kit flag <kitname> <flag> hand");
			return;
		}

		try {
			kit.setFlag(flag, world, flag.parseInput(value));
		} catch (InvalidFlagValueException e) {
			sender.sendMessage(e.getMessages());
			return;
		}

		sendMessage(sender, getMessage("flagSet", flag.getName(), value, kit.getDisplayName(world), world));
	}
}