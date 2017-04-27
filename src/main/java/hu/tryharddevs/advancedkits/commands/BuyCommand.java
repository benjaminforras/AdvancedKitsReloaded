package hu.tryharddevs.advancedkits.commands;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.CommandManager;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.utils.VaultUtil;
import hu.tryharddevs.advancedkits.utils.menuapi.components.ActionListener;
import hu.tryharddevs.advancedkits.utils.menuapi.components.Menu;
import hu.tryharddevs.advancedkits.utils.menuapi.components.MenuObject;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@SuppressWarnings("ConstantConditions")
public class BuyCommand implements ActionListener
{
	private static BuyCommand buyInventoryListener = new BuyCommand();

	@CommandManager.Cmd(cmd = "buy", help = "Buy kit", longhelp = "This command opens up a gui where you can buy kits.", permission = "buy", args = "[kitname]", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished buyCommand(CommandSender sender, Object[] args)
	{
		Player player = (Player) sender;
		User   user   = User.getUser(player.getUniqueId());
		String world  = player.getWorld().getName();

		if (args.length == 0) {
			Inventory inventory = Bukkit.createInventory(player, ((int) (Math.ceil((double) KitManager.getKits().size() / 9)) * 9), "AdvancedKitsReborn - Buy kit");
			Menu      menu      = new Menu(inventory);

			MenuObject menuObject;
			for (Kit kit : KitManager.getKits()) {
				if (!kit.getFlag(VISIBLE, world)) continue;
				if (kit.getFlag(FREE, world) || user.isUnlocked(kit)) continue;

				menuObject = new MenuObject(kit.getFlag(ICON, world), (byte) 0, ChatColor.RED + kit.getDisplayName(player.getWorld().getName()), KitManager.getKitDescription(player, kit, world));
				menuObject.setActionListener(buyInventoryListener);

				menu.addMenuObject(menuObject);
			}
			menu.openForPlayer(player);

			return CommandManager.CommandFinished.DONE;
		}

		Kit kit = KitManager.getKit(String.valueOf(args[0]), world);
		if (Objects.isNull(kit)) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return CommandManager.CommandFinished.DONE;
		}

		if(!player.hasPermission(kit.getPermission()))
		{
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("noKitPermission"));
			return CommandManager.CommandFinished.DONE;
		}


		EconomyResponse r = VaultUtil.getEconomy().withdrawPlayer(player, kit.getFlag(COST, world));
		if (r.transactionSuccess()) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyBought", kit.getDisplayName(world)));

			user.addToUnlocked(kit);
			user.save();

			if (kit.getFlag(USEONBUY, world)) Bukkit.dispatchCommand(player, "kit use " + kit.getName());
		}
		else {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughMoney", r.amount));
		}


		return CommandManager.CommandFinished.DONE;
	}

	@Override
	public void onClick(ClickType clickType, MenuObject menuObject, Player whoClicked)
	{
		ItemStack clickedItem = menuObject.toItemStack();
		if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

		Kit kit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), whoClicked.getWorld().getName());
		if (Objects.isNull(kit)) {
			whoClicked.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return;
		}

		Bukkit.dispatchCommand(whoClicked, "kit buy " + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
		menuObject.getMenu().close(whoClicked);
	}
}
