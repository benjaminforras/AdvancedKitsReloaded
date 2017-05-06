package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.Config;
import hu.tryharddevs.advancedkits.cinventory.inventories.CPageInventory;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.utils.ItemBuilder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.USEONBUY;
import static hu.tryharddevs.advancedkits.utils.MessagesApi.sendMessage;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@CommandAlias("kit|akit|advancedkits|kits|akits")
public class BuyCommand extends BaseCommand {
	private final AdvancedKitsMain instance;

	public BuyCommand(AdvancedKitsMain instance) {
		this.instance = instance;
	}

	@Subcommand("buy")
	@CommandPermission("advancedkits.buy")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onBuyCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();

		if(Config.DISABLED_WORLDS.contains(world)) {
			sendMessage(player, getMessage("kitUseDisabledInWorld"));
			return;
		}

		if (Objects.isNull(kit)) {

			CPageInventory cPageInventory = new CPageInventory("AdvancedKits - Buy Kit", player);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world) && (!_kit.getFlag(FREE, world) && !user.isUnlocked(_kit))).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
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
				Bukkit.dispatchCommand(_player, "akit buy " + clickedKit.getName());
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

			if (kit.getFlag(USEONBUY, world)) Bukkit.dispatchCommand(player, "akit use " + kit.getName());
		} else {
			sendMessage(player, getMessage("notEnoughMoney", r.amount));
		}
	}
}
