package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.MenuBuilder.inventory.InventoryMenuBuilder;
import hu.tryharddood.advancedkits.Utils.ItemBuilder;
import hu.tryharddood.advancedkits.Utils.PageLayout;
import hu.tryharddood.advancedkits.Utils.Title;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hu.tryharddood.advancedkits.Utils.I18n.tl;
import static hu.tryharddood.advancedkits.Utils.ItemBuilder.*;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class CreateCommand extends Subcommand {

	private static List<ItemStack> filling = new ArrayList<>(Arrays.asList(
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.LIGHT_BLUE.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Helmet Here").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.LIGHT_BLUE.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Chestplate Here").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.LIGHT_BLUE.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Leggings Here").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.LIGHT_BLUE.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Boots Here").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build()));

	@Override
	public String getPermission() {
		return Variables.KITADMIN_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit create <kit>";
	}

	@Override
	public String getDescription() {
		return "Creates a kit with the given name.";
	}

	@Override
	public int getArgs() {
		return 2;
	}

	@Override
	public boolean playerOnly() {
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		Kit    kit    = AdvancedKits.getKitManager().getKit(args[1]);
		if (kit != null)
		{
			player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + tl("error_kit_create_exists"));
			return;
		}

		InventoryMenuBuilder imb = new InventoryMenuBuilder().withSize(54).withTitle("Craete Kit - " + args[1]);

		PageLayout pl = new PageLayout("XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"OOOOXXXXX",
				"OOOOOOOOO",
				"OOOOOOOOO");

		if (filling.size() == 21)
			filling.add(new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + tl("gui_button_create")).build());

		ItemStack[] items = pl.generate(filling.toArray(new ItemStack[filling.size()]));
		imb.withItems(items);

		imb.show(player);

		imb.onInteract((player1, action, slot) ->
		{
			ItemStack clickedItem = imb.getInventory().getItem(slot);

			if (clickedItem != null && clickedItem.getType().equals(Material.STAINED_GLASS_PANE) && clickedItem.getDurability() == (short) 13 && clickedItem.hasItemMeta() && clickedItem.getItemMeta().getDisplayName().contains(tl("gui_button_create", true)))
			{
				Inventory       inventory  = imb.getInventory();
				List<ItemStack> itemStacks = new ArrayList<>();

				for (int i = 0; i < 36; i++)
				{
					if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR || i == 27 || i == 28 || i == 29 || i == 30)
					{
						continue;
					}
					else
					{
						itemStacks.add(inventory.getItem(i));
					}
				}

				List<ItemStack> armors = new ArrayList<>();
				for (int i = 27; i < 31; i++)
				{
					if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR || inventory.getItem(i).getType() == Material.STAINED_GLASS_PANE)
					{
						continue;
					}

					if (i == 27 && isHelmet(inventory.getItem(i).getType()))
					{
						armors.add(inventory.getItem(i));
					}
					else if (i == 28 && isChestplate(inventory.getItem(i).getType()))
					{
						armors.add(inventory.getItem(i));
					}
					else if (i == 29 && isLeggings(inventory.getItem(i).getType()))
					{
						armors.add(inventory.getItem(i));
					}
					else if (i == 30 && isBoots(inventory.getItem(i).getType()))
					{
						armors.add(inventory.getItem(i));
					}
				}

				new Kit(args[1]).createKit(itemStacks, armors);
				AdvancedKits.getKitManager().load();

				player.closeInventory();
				Title.sendTitle(player, 2, 20, 2, "", ChatColor.GREEN + tl("kit_create"));
			}

			if (clickedItem != null && clickedItem.getType().equals(Material.STAINED_GLASS_PANE) && (clickedItem.getDurability() == (short) 15 || clickedItem.getDurability() == (short) 14 || clickedItem.getDurability() == (short) 13))
				return;

			if (slot == 27 || slot == 28 || slot == 29 || slot == 30)
			{
				ItemStack item = player.getItemOnCursor();
				if (item == null || item.getType() == Material.AIR)
				{
					if (slot == 27)
						imb.getInventory().setItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.LIGHT_BLUE.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Helmet Here").build());
					if (slot == 28)
						imb.getInventory().setItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.LIGHT_BLUE.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Chestplate Here").build());
					if (slot == 29)
						imb.getInventory().setItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.LIGHT_BLUE.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Leggings Here").build());
					if (slot == 30)
						imb.getInventory().setItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.LIGHT_BLUE.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Boots Here").build());

					if (clickedItem != null && clickedItem.getType() != Material.STAINED_GLASS_PANE)
						player.setItemOnCursor(clickedItem);
				}
				else if (isHelmet(item.getType()))
				{
					imb.getInventory().setItem(27, player.getItemOnCursor());
					player.setItemOnCursor(null);
				}
				else if (isChestplate(item.getType()))
				{
					imb.getInventory().setItem(28, player.getItemOnCursor());
					player.setItemOnCursor(null);
				}
				else if (isLeggings(item.getType()))
				{
					imb.getInventory().setItem(29, player.getItemOnCursor());
					player.setItemOnCursor(null);
				}
				else if (isBoots(item.getType()))
				{
					imb.getInventory().setItem(30, player.getItemOnCursor());
					player.setItemOnCursor(null);
				}
			}
			else
			{
				ItemStack item = player.getItemOnCursor();

				if (item == null || item.getType() == Material.AIR)
				{
					imb.getInventory().setItem(slot, new ItemStack(Material.AIR));
					if (clickedItem != null && (clickedItem.getType() != Material.STAINED_GLASS_PANE || clickedItem.getType() != Material.AIR))
						player.setItemOnCursor(clickedItem);
				}
				else
				{
					imb.getInventory().setItem(slot, item);
					player.setItemOnCursor(clickedItem);
				}
			}
			imb.refreshContent();
		}, ClickType.LEFT);
	}
}
