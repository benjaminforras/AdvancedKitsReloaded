package advancedkits.Commands.SubCommands;

import advancedkits.AdvancedKits;
import advancedkits.Commands.Subcommand;
import advancedkits.InventoryApi.ItemBuilder;
import advancedkits.Kits.Kit;
import advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class CreateCommand extends Subcommand
{
	@Override
	public String getPermission()
	{
		return Variables.KITADMIN_PERMISSION;
	}

	@Override
	public String getUsage()
	{
		return "/kit create <kit>";
	}

	@Override
	public String getDescription()
	{
		return "Creates a kit with the given name.";
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
		Kit    kit    = AdvancedKits.getKitManager().getKit(args[1]);
		if (kit != null)
		{
			player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + tl("error_kit_create_exists"));
			return;
		}

		int       inventorySize = 54;
		Inventory inventory     = Bukkit.createInventory(null, inventorySize, "Create - " + args[1]);

		inventory.setItem(inventorySize - 4, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_create"))).build());
		inventory.setItem(inventorySize - 6, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setTitle(ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_cancel"))).build());

		for (int i = 27; i < 31; i++)
		{
			inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
		}

		for (int i = 36; i < 45; i++)
		{
			inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
		}
		player.openInventory(inventory);
	}
}
