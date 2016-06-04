package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.InventoryApi.ItemBuilder;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static hu.tryharddood.advancedkits.Listeners.InventoryListener.*;
import static hu.tryharddood.advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class EditCommand extends Subcommand
{
	@Override
	public String getPermission()
	{
		return Variables.KITADMIN_PERMISSION;
	}

	@Override
	public String getUsage()
	{
		return "/kit edit <kit>";
	}

	@Override
	public String getDescription()
	{
		return "Edits a kit.";
	}

	@Override
	public int getArgs()
	{
		return 2;
	}

	@Override
	public boolean playerOnly()
	{
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player player = (Player) sender;
		Kit kit = AdvancedKits.getKitManager().getKit(args[1]);
		if (kit == null)
		{
			sendMessage(player, tl("error_kit_not_found"), ChatColor.RED);
			return;
		}

		int inventorySize = 54;
		Inventory inventory = Bukkit.createInventory(null, inventorySize, "Edit - " + kit.getName());

		kit.getItemStacks().forEach(inventory::addItem);

		List<ItemStack> armor = kit.getArmor();
		for (ItemStack i : armor)
		{
			if (isHelmet(i.getType()))
			{
				inventory.setItem(27, i);
			}
			else if (isChestplate(i.getType()))
			{
				inventory.setItem(28, i);
			}
			else if (isLeggings(i.getType()))
			{
				inventory.setItem(29, i);
			}
			else if (isBoots(i.getType()))
			{
				inventory.setItem(30, i);
			}
		}

		for (int i = 27; i < 31; i++)
		{
			if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR)
			{
				inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
			}
		}

		inventory.setItem(inventorySize - 4, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_edit"))).build());
		inventory.setItem(inventorySize - 6, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setTitle(ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_cancel"))).build());

		for (int i = 36; i < 45; i++)
		{
			inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
		}

		player.openInventory(inventory);
	}
}
