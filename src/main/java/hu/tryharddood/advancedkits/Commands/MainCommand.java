package hu.tryharddood.advancedkits.Commands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.MenuBuilder.ItemBuilder;
import hu.tryharddood.advancedkits.MenuBuilder.PageInventory;
import hu.tryharddood.advancedkits.MenuBuilder.inventory.InventoryMenuBuilder;
import hu.tryharddood.advancedkits.MenuBuilder.inventory.InventoryMenuListener;
import hu.tryharddood.advancedkits.Utils.PageLayout;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static hu.tryharddood.advancedkits.MenuBuilder.ItemBuilder.*;
import static hu.tryharddood.advancedkits.Utils.Localization.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class MainCommand extends Subcommand {
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
			new ItemBuilder(Material.BOOK_AND_QUILL).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_edit"))).build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 14).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_buy"))).build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 0).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_back"))).build(),
			new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 13).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_use"))).build(),
			new ItemBuilder(Material.BARRIER).setTitle(ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_delete"))).build()
	));

	private static Kit kit;

	private static InventoryMenuListener viewInventoryListener = new InventoryMenuListener() {
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event) {
			if (event.getCurrentItem() == null) return;

			ItemStack clicked = event.getCurrentItem();
			if (clicked.getType() == Material.STAINED_GLASS_PANE)
			{
				if (clicked.getDurability() == (short) 14) //BUY
				{
					player.closeInventory();
					Bukkit.dispatchCommand(player, "kit buy " + kit.getName());
				}
				else if (clicked.getDurability() == (short) 0) // INFO
				{
					player.closeInventory();
					Bukkit.dispatchCommand(player, "kit");
				}
				else if (clicked.getDurability() == (short) 13) // USE
				{
					player.closeInventory();
					Bukkit.dispatchCommand(player, "kit use " + kit.getName());
				}
			}
			else if (clicked.getType() == Material.BOOK_AND_QUILL)
			{
				player.closeInventory();
				Bukkit.dispatchCommand(player, "kit edit " + kit.getName());
			}
			else if (clicked.getType() == Material.BARRIER)
			{
				player.closeInventory();
				Bukkit.dispatchCommand(player, "kit delete " + kit.getName());
			}
		}
	};

	public static void openViewInventory(Player player, Kit kit) {
		if (!player.hasPermission(Variables.KITADMIN_PERMISSION))
		{
			filling.set(13, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
			filling.set(17, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 8).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
		}
		MainCommand.kit = kit;

		InventoryMenuBuilder imb = new InventoryMenuBuilder().withSize(54).withTitle("Kit View - " + kit.getDisplayName());

		List<ItemStack> itemStackList = kit.getItemStacks();
		for (int i = 0; i < itemStackList.size(); i++)
		{
			imb.withItem(i, itemStackList.get(i));
		}

		PageLayout pl = new PageLayout("XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"OOOOXXXXX",
				"OOOOOOOOO",
				"OXOXOXOXO");

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
		imb.onInteract(viewInventoryListener, ClickType.LEFT);
	}

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

		List<Kit>    kits = AdvancedKits.getKitManager().getKits().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
		List<String> lore = new ArrayList<>();

		ArrayList<ItemStack> items = new ArrayList<>();
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
			lore.clear();

			if (!AdvancedKits.getKitManager().CheckCooldown(player, kitItem))
			{
				lore.add("§8");
				lore.add(ChatColor.RED + "" + ChatColor.BOLD + tl("next_use") + ":");
				lore.add(ChatColor.WHITE + "" + ChatColor.BOLD + "- " + AdvancedKits.getKitManager().getDelay(player, kitItem));
				lore.add("§8");
			}
			items.add(new ItemBuilder(kitItem.getIcon()).setTitle(ChatColor.translateAlternateColorCodes('&', kitItem.getDisplayName())).addLores(lore).addLores(AdvancedKits.getKitManager().getLores(player, kitItem)).build());
		}
		PageInventory pageInventory = new PageInventory(tl("gui_title"), items);
		pageInventory.show(player);

		pageInventory.onInteract(new InventoryMenuListener() {
			@Override
			public void interact(Player player, ClickType action, InventoryClickEvent event) {
				if (event.getCurrentItem() == null)
					return;

				ItemStack item    = event.getCurrentItem();
				int       newPage = 0;
				if (item.equals(pageInventory.getBackPage()))
				{
					newPage = -1;
				}
				else if (item.equals(pageInventory.getForwardsPage()))
				{
					newPage = 1;
				}
				if (newPage != 0)
				{
					pageInventory.setPage(pageInventory.getCurrentPage() + newPage);
					return;
				}
				if (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) return;
				Kit kit = AdvancedKits.getKitManager().getKitByDisplayName(item.getItemMeta().getDisplayName().replaceAll("§", "&"));
				openViewInventory(player, kit);
			}
		}, ClickType.LEFT);
	}
}
