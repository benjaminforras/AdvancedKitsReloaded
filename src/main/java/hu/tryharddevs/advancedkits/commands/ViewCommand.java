package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.Config;
import hu.tryharddevs.advancedkits.cinventory.inventories.CPageInventory;
import hu.tryharddevs.advancedkits.cinventory.inventories.CSimpleInventory;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.utils.MessagesApi.sendMessage;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@CommandAlias("kit|akit|advancedkits|kits|akits")
public class ViewCommand extends BaseCommand {
	private final AdvancedKitsMain instance;

	public ViewCommand(AdvancedKitsMain instance) {
		this.instance = instance;
	}

	@Subcommand("view")
	@CommandPermission("advancedkits.view")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onViewCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();

		if(Config.DISABLED_WORLDS.contains(world)) {
			sendMessage(player, getMessage("kitUseDisabledInWorld"));
			return;
		}

		if (Objects.isNull(kit)) {
			CPageInventory cPageInventory = new CPageInventory("AdvancedKits - View Kit", player);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world)).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).hideAttributes().toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			cPageInventory.openInventory();

			cPageInventory.onInventoryClickEvent((_event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_player.closeInventory();
				Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit view " + clickedKit.getName());
			});
			return;
		}

		CSimpleInventory cSimpleInventory = new CSimpleInventory("AdvancedKits - View Kit", player, 54);
		cSimpleInventory.addItems(kit.getItems());

		int i = 36;
		for (ItemStack armor : kit.getArmors()) {
			cSimpleInventory.setItem(i, armor);
			i++;
		}

		cSimpleInventory.setItem(49, new ItemBuilder(Material.PAPER).setName(getMessage("informations")).setLore(KitManager.getKitDescription(player, kit, world)).toItemStack());

		if (user.isUnlocked(kit) || kit.getFlag(FREE, world)) {
			cSimpleInventory.setItem(53, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 13).setName(ChatColor.GREEN + getMessage("guiUse")).hideAttributes().toItemStack());
		} else if (kit.getFlag(COST, world) > 0) {
			cSimpleInventory.setItem(53, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 14).setName(ChatColor.GREEN + getMessage("guiBuy")).hideAttributes().toItemStack());
		}

		cSimpleInventory.setItem(45, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short)0).setName(ChatColor.GREEN + getMessage("guiBackToMenu")).hideAttributes().toItemStack());
		cSimpleInventory.openInventory();

		cSimpleInventory.onInventoryClickEvent((_event) -> {
			ItemStack clickedItem = _event.getCurrentItem();
			if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
				return;

			Player _player = (Player) _event.getWhoClicked();
			if (clickedItem.getType() == Material.STAINED_GLASS_PANE) {
				if (clickedItem.getDurability() == (short) 13) {
					Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit use " + kit.getName());
				} else if (clickedItem.getDurability() == (short) 14) {
					Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit buy " + kit.getName());
				} else if (clickedItem.getDurability() == (short) 0) {
					Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit view");
				}
			}
		});
		sendMessage(player, getMessage("kitView", kit.getDisplayName(world)));
	}
}
