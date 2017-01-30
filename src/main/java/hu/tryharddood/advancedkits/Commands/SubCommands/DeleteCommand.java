package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.MenuBuilder.ItemBuilder;
import hu.tryharddood.advancedkits.MenuBuilder.inventory.InventoryMenuBuilder;
import hu.tryharddood.advancedkits.Utils.PageLayout;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hu.tryharddood.advancedkits.Utils.Localization.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class DeleteCommand extends Subcommand {
	private static List<ItemStack> filling = new ArrayList<>(Arrays.asList(
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 15).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 15).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 13).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Cancel").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 15).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 15).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 15).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 14).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Delete").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 15).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 15).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build()
	));

	@Override
	public String getPermission() {
		return Variables.KITADMIN_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit delete <kit>";
	}

	@Override
	public String getDescription() {
		return "Deletes a kit.";
	}

	@Override
	public int getArgs() {
		return 2;
	}

	@Override
	public boolean playerOnly() {
		return false;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Kit kit = AdvancedKits.getKitManager().getKit(args[1]);
		if (kit == null)
		{
			sendMessage(sender, tl("error_kit_not_found"), ChatColor.RED);
			return;
		}

		if (sender instanceof Player)
		{
			Player               player      = (Player) sender;
			InventoryMenuBuilder mbInventory = new InventoryMenuBuilder(9, "Are you sure?");

			PageLayout pl = new PageLayout("OOOOOOOOO");

			ItemStack[] items = pl.generate(filling.toArray(new ItemStack[filling.size()]));
			mbInventory.withItems(items);

			mbInventory.show(player);
			mbInventory.onInteract((player1, action, event) ->
			{
				if (event.getCurrentItem() == null)
					return;

				ItemStack item = event.getCurrentItem();
				if (item.getDurability() == (short) 13) //Cancel
				{
					player.closeInventory();
				}
				else if (item.getDurability() == (short) 14) //Delete
				{
					AdvancedKits.getKitManager().deleteKit(kit);
					player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + tl("kit_delete"));
				}
			}, ClickType.LEFT);
		}
		else
		{
			AdvancedKits.getKitManager().deleteKit(kit);
			sender.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + tl("kit_delete"));
		}
	}
}
