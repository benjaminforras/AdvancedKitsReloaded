package hu.tryharddevs.advancedkits.commands;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.CommandManager;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.utils.menuapi.components.ActionListener;
import hu.tryharddevs.advancedkits.utils.menuapi.components.Coordinates;
import hu.tryharddevs.advancedkits.utils.menuapi.components.Menu;
import hu.tryharddevs.advancedkits.utils.menuapi.components.MenuObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.ICON;
import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.VISIBLE;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public class ViewCommand implements ActionListener
{
	private static ViewCommand viewInventoryListener = new ViewCommand();

	@CommandManager.Cmd(cmd = "view", help = "View kits", longhelp = "This command opens up a gui where you can view kits.", permission = "view", args = "[kitname]", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished viewCommand(CommandSender sender, Object[] args)
	{
		Player player = (Player) sender;
		User   user   = User.getUser(player.getUniqueId());
		String world  = player.getWorld().getName();

		if (args.length == 0) {
			Inventory inventory = Bukkit.createInventory(player, ((int) (Math.ceil((double) KitManager.getKits().size() / 9)) * 9), "AdvancedKitsReborn - View kit");
			Menu      menu      = new Menu(inventory);

			MenuObject menuObject;
			for (Kit kit : KitManager.getKits()) {
				if (!kit.getFlag(VISIBLE, world)) continue;

				menuObject = new MenuObject(kit.getFlag(ICON, world), (byte) 0, ChatColor.WHITE + kit.getDisplayName(player.getWorld().getName()), KitManager.getKitDescription(player, kit, world));
				menuObject.setActionListener(viewInventoryListener);

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

		Inventory inventory = Bukkit.createInventory(player, 54, "AdvancedKitsReborn - View kit");
		Menu      menu      = new Menu(inventory);

		MenuObject menuObject;
		for (ItemStack itemStack : kit.getItems()) {
			menuObject = new MenuObject(itemStack);
			menu.addMenuObject(menuObject);
		}

		int x = 1;
		for (ItemStack itemStack : kit.getArmors()) {
			menuObject = new MenuObject(itemStack);
			menu.setMenuObjectAt(new Coordinates(menu, x, 5), menuObject);
			x++;
		}
		menuObject = new MenuObject(Material.PAPER, (byte) 0, getMessage("informations"), KitManager.getKitDescription(player, kit, world));
		menu.setMenuObjectAt(new Coordinates(menu, 5, 6), menuObject);

		menu.openForPlayer(player);
		player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitView", kit.getDisplayName(world)));

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

		menuObject.getMenu().close(whoClicked);
		Bukkit.dispatchCommand(whoClicked, "kit view " + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
	}
}
