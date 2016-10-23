package hu.tryharddood.advancedkits.Commands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.MenuBuilder.inventory.InventoryMenuBuilder;
import hu.tryharddood.advancedkits.Utils.ItemBuilder;
import hu.tryharddood.advancedkits.Utils.PageLayout;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static hu.tryharddood.advancedkits.Utils.I18n.tl;
import static hu.tryharddood.advancedkits.Utils.ItemBuilder.*;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class MainCommand extends Subcommand {
	private static List<ItemStack> filling = Arrays.asList(
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
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 14).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_buy"))).build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 0).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_back"))).build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 13).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_use"))).build()
	);

	@Override
	public String getPermission() {
		return Variables.KIT_USE_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit";
	}

	@Override
	public String getDescription() {
		return "Opens up the kit GUI";
	}

	@Override
	public int getArgs() {
		return 0;
	}

	@Override
	public boolean playerOnly() {
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;

		InventoryMenuBuilder mbInventory = new InventoryMenuBuilder().withType(InventoryType.PLAYER).withTitle("AdvancedKitsReloaded - v${project.version}");

		List<Kit>    kits = AdvancedKits.getKitManager().getKits().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
		List<String> lore = new ArrayList<>();

		int i = -1;
		for (Kit kitItem : kits)
		{
			if (!kitItem.isVisible() && (!player.hasPermission(Variables.KITADMIN_PERMISSION) && AdvancedKits.getKitManager().getUses(kitItem, player) >= kitItem.getUses()))
			{
				continue;
			}

			if (!player.hasPermission(Variables.KIT_USE_KIT_PERMISSION_ALL))
			{
				if (!player.hasPermission(kitItem.getPermission()) && !player.hasPermission(Variables.KITADMIN_PERMISSION))
				{
					continue;
				}
			}
			i++;

			lore.clear();

			if (!AdvancedKits.getKitManager().CheckCooldown(player, kitItem))
			{
				lore.add("§8");
				lore.add(ChatColor.RED + "" + ChatColor.BOLD + tl("kituse_wait").replaceAll("\\{(\\d*?)\\}", "") + ":");
				lore.add(ChatColor.WHITE + "" + ChatColor.BOLD + "- " + AdvancedKits.getKitManager().getDelay(player, kitItem));
				lore.add("§8");
			}

			mbInventory.withItem(i, new ItemBuilder(kitItem.getIcon()).setTitle(ChatColor.translateAlternateColorCodes('&', kitItem.getDisplayName())).addLores(lore).addLores(AdvancedKits.getKitManager().getLores(player, kitItem)).build());
		}

		mbInventory.show(player);
		mbInventory.onInteract((player1, action, slot) ->
		{
			if (mbInventory.getInventory().getItem(slot) == null)
				return;

			ItemStack item = mbInventory.getInventory().getItem(slot);
			Kit       kit  = AdvancedKits.getKitManager().getKitByDisplayName(item.getItemMeta().getDisplayName().replaceAll("§", "&"));
			openViewInventory(player1, kit);
		}, ClickType.LEFT);
	}

	private void openViewInventory(Player player, Kit kit) {
		InventoryMenuBuilder imb = new InventoryMenuBuilder().withSize(54).withTitle("Kit View - " + kit.getDisplayName());

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
				"XXOXOXOXX");

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

		imb.onInteract((player2, action1, slot1) ->
		{
			if (imb.getInventory().getItem(slot1) == null) return;

			ItemStack clicked = imb.getInventory().getItem(slot1);
			if (clicked.getType() == Material.STAINED_GLASS_PANE)
			{
				if (clicked.getDurability() == (short) 14) //BUY
				{
					Bukkit.dispatchCommand(player, "kit buy " + kit.getName());
				}
				else if (clicked.getDurability() == (short) 0) // INFO
				{
					Bukkit.dispatchCommand(player, "kit");
				}
				else if (clicked.getDurability() == (short) 13) // USE
				{
					Bukkit.dispatchCommand(player, "kit use " + kit.getName());
				}
			}
		}, ClickType.LEFT);
	}
}
