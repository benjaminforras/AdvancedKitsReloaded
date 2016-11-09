package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.MenuBuilder.ItemBuilder;
import hu.tryharddood.advancedkits.MenuBuilder.inventory.InventoryMenuBuilder;
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

import static hu.tryharddood.advancedkits.MenuBuilder.ItemBuilder.*;
import static hu.tryharddood.advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class EditCommand extends Subcommand {

	private static List<ItemStack> filling = new ArrayList<>(Arrays.asList(
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 3).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Helmet Here").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 3).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Chestplate Here").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 3).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Leggings Here").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 3).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Boots Here").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build()));

	@Override
	public String getPermission() {
		return Variables.KITADMIN_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit edit <kit>";
	}

	@Override
	public String getDescription() {
		return "Edits a kit.";
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
		if (kit == null)
		{
			sendMessage(player, tl("error_kit_not_found"), ChatColor.RED);
			return;
		}

		InventoryMenuBuilder imb = new InventoryMenuBuilder().withSize(54).withTitle("Edit Kit - " + kit.getDisplayName());

		List<ItemStack> itemStackList = kit.getItemStacks();
		int             i1            = -1;
		for (ItemStack itemData : itemStackList)
		{
			i1++;
			imb.withItem(i1, itemData);
		}

		PageLayout pl = new PageLayout("XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"OOOOXXXXX",
				"OOOOOOOOO",
				"OOOOOOOOO");

		if (filling.size() == 21)
			filling.add(new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + tl("gui_button_edit")).build());

		ItemStack[] items = pl.generate(filling.toArray(new ItemStack[filling.size()]));
		imb.withItems(items);

		List<ItemStack> armor = kit.getArmor();
		for (ItemStack i : armor)
		{
			if (isHelmet(i.getType()))
			{
				imb.withItem(27, i);
			}
			else if (isChestplate(i.getType()))
			{
				imb.withItem(28, i);
			}
			else if (isLeggings(i.getType()))
			{
				imb.withItem(29, i);
			}
			else if (isBoots(i.getType()))
			{
				imb.withItem(30, i);
			}
		}

		imb.show(player);

		imb.onInteract((player1, action, event) ->
		{
			ItemStack clickedItem = event.getCurrentItem();
			int       slot        = event.getSlot();

			if (clickedItem != null && clickedItem.getType().equals(Material.STAINED_GLASS_PANE) && clickedItem.getDurability() == (short) 13 && clickedItem.hasItemMeta() && clickedItem.getItemMeta().getDisplayName().contains(tl("gui_button_edit", true)))
			{
				Inventory inventory = imb.getInventory();
				kit.setSave(true);
				kit.getItemStacks().clear();
				for (int i = 0; i < 36; i++)
				{
					if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR || i == 27 || i == 28 || i == 29 || i == 30)
					{
						continue;
					}
					kit.AddItem(inventory.getItem(i));
				}

				ArrayList<ItemStack> armors = new ArrayList<>();
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
				kit.setArmor(armors);
				kit.setSave(false);
				AdvancedKits.getKitManager().load();

				player.closeInventory();
				Title.sendTitle(player, 2, 20, 2, "", ChatColor.GREEN + tl("kit_edit"));
			}

			if (clickedItem != null && clickedItem.getType().equals(Material.STAINED_GLASS_PANE) && (clickedItem.getDurability() == (short) 8 || clickedItem.getDurability() == (short) 14 || clickedItem.getDurability() == (short) 13))
				return;

			if (slot == 27 || slot == 28 || slot == 29 || slot == 30)
			{
				ItemStack item = player.getItemOnCursor();
				if (item == null || item.getType() == Material.AIR)
				{
					if (slot == 27)
						imb.getInventory().setItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 3).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Helmet Here").build());
					if (slot == 28)
						imb.getInventory().setItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 3).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Chestplate Here").build());
					if (slot == 29)
						imb.getInventory().setItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 3).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Leggings Here").build());
					if (slot == 30)
						imb.getInventory().setItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 3).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Place Boots Here").build());

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
