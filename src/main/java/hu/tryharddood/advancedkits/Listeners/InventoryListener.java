package hu.tryharddood.advancedkits.Listeners;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.InventoryApi.ItemBuilder;
import hu.tryharddood.advancedkits.InventoryApi.PageInventory;
import hu.tryharddood.advancedkits.InventoryApi.events.PagesClickEvent;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Utils.Title;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hu.tryharddood.advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class InventoryListener implements Listener {

	public static boolean isBoots(Material type) {
		return type == Material.DIAMOND_BOOTS || type == Material.IRON_BOOTS || type == Material.GOLD_BOOTS || type == Material.CHAINMAIL_BOOTS || type == Material.LEATHER_BOOTS;
	}

	public static boolean isChestplate(Material type) {
		return type == Material.DIAMOND_CHESTPLATE || type == Material.IRON_CHESTPLATE || type == Material.GOLD_CHESTPLATE || type == Material.CHAINMAIL_CHESTPLATE || type == Material.LEATHER_CHESTPLATE;
	}

	public static boolean isHelmet(Material type) {
		return type == Material.DIAMOND_HELMET || type == Material.SKULL_ITEM || type == Material.PUMPKIN || type == Material.IRON_HELMET || type == Material.GOLD_HELMET || type == Material.CHAINMAIL_HELMET || type == Material.LEATHER_HELMET;
	}

	public static boolean isLeggings(Material type) {
		return type == Material.DIAMOND_LEGGINGS || type == Material.IRON_LEGGINGS || type == Material.GOLD_LEGGINGS || type == Material.CHAINMAIL_LEGGINGS || type == Material.LEATHER_LEGGINGS;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
		{
			return;
		}

		Player player  = (Player) event.getWhoClicked();
		String invName = event.getInventory().getTitle();

		if (invName.contains("Create"))
		{
			CreateInventory(player, event, invName);
		}

		if (invName.contains("Edit"))
		{
			EditInventory(player, new Kit(invName.substring(7)), event);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPageClickEvent(PagesClickEvent event) {
		if (event.getItemStack() == null || event.getItemStack().getType() == Material.AIR || !event.getItemStack().hasItemMeta() || !event.getItemStack().getItemMeta().hasDisplayName())
		{
			event.setCancelled(true);
			return;
		}

		Player    player    = event.getPlayer();
		ItemStack itemStack = event.getItemStack();
		String    itemName  = event.getItemStack().getItemMeta().getDisplayName();
		String    invName   = event.getInventory().getTitle();

		if (invName.contains("Kits") && itemStack.getType() != Material.PAPER)
		{
			Kit kit = AdvancedKits.getKitManager().getKitByDisplayName(itemName.replaceAll("§", "&"));
			if (kit == null)
			{
				return;
			}
			List<ItemStack> itemStackList = kit.getItemStacks();

			int inventorySize = 54;

			PageInventory inv   = new PageInventory(player);
			ItemStack[]   items = itemStackList.toArray(new ItemStack[inventorySize]);

			if (player.hasPermission(Variables.KITADMIN_PERMISSION))
			{
				items[inventorySize - 9] = new ItemBuilder(Material.BOOK_AND_QUILL).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_edit"))).build();
				items[inventorySize - 1] = new ItemBuilder(Material.BARRIER).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_delete"))).build();
			}

			{//TODO
				if (AdvancedKits.getKitManager().canUse(player, kit))
				{
					items[inventorySize - 4] = new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 13).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_use"))).build();
				}

				items[inventorySize - 5] = new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 0).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_back"))).addLores(AdvancedKits.getKitManager().getLores(player, kit)).build();

				if (AdvancedKits.getKitManager().canBuy(player, kit))
				{
					items[inventorySize - 6] = new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 14).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_buy"))).build();
				}
			}

			List<ItemStack> armor = kit.getArmor();
			for (ItemStack i : armor)
			{
				if (isHelmet(i.getType()))
				{
					items[27] = i;
				}
				else if (isChestplate(i.getType()))
				{
					items[28] = i;
				}
				else if (isLeggings(i.getType()))
				{
					items[29] = i;
				}
				else if (isBoots(i.getType()))
				{
					items[30] = i;
				}
			}

			for (int i = 36; i < 45; i++)
			{
				items[i] = new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build();
			}

			inv.setPages(items);
			inv.setTitle("Details - " + kit.getName());
			inv.openInventory();
		}

		if (invName.contains("Details"))
		{
			Kit kit = AdvancedKits.getKitManager().getKit(invName.substring(10));
			if (kit == null)
			{
				return;
			}

			if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_use"))))
			{
				Bukkit.dispatchCommand(player, "kit use " + kit.getName());
			}

			if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_buy"))))
			{
				Bukkit.dispatchCommand(player, "kit buy " + kit.getName());
			}

			if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_back"))))
			{
				Bukkit.dispatchCommand(player, "kit");
			}

			if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_edit"))))
			{
				Bukkit.dispatchCommand(player, "kit edit " + kit.getName());
			}

			if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_delete"))))
			{
				Bukkit.dispatchCommand(player, "kit delete " + kit.getName());
			}
			event.setCancelled(true);
		}
	}

	private void CreateInventory(Player player, InventoryClickEvent event, String invName) {
		Kit kit = AdvancedKits.getKitManager().getKit(invName.substring(9));
		if (kit != null)
		{
			player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + tl("error_kit_create_exists"));
			return;
		}

		ItemStack itemStack = event.getCurrentItem();
		Inventory inventory = event.getInventory();

		List<ItemStack> itemStacks = new ArrayList<>();
		if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
		{
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
			if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_create"))))
			{

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

				new Kit(invName.substring(9)).createKit(itemStacks, armors);
				event.setCancelled(true);
				player.closeInventory();

				Title.sendTitle(player, 2, 20, 2, "", ChatColor.RED + tl("kit_create"));
			}

			if (itemName.equalsIgnoreCase(ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_cancel"))))
			{
				event.setCancelled(true);
				player.closeInventory();
			}
		}
		if (Arrays.asList(27, 28, 29, 30).contains(event.getRawSlot()))
		{
			if (event.getCursor() == null || event.getCursor().getType() == Material.AIR)
			{
				event.setCurrentItem(new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 13).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
			}

			if (event.getRawSlot() == 27)
			{
				if (isHelmet(event.getCursor().getType()))
				{
					event.setCurrentItem(event.getCursor());
					player.setItemOnCursor(null);
				}
			}
			else if (event.getRawSlot() == 28)
			{
				if (isChestplate(event.getCursor().getType()))
				{
					event.setCurrentItem(event.getCursor());
					player.setItemOnCursor(null);
				}
			}
			else if (event.getRawSlot() == 29)
			{
				if (isLeggings(event.getCursor().getType()))
				{
					event.setCurrentItem(event.getCursor());
					player.setItemOnCursor(null);
				}
			}
			else if (event.getRawSlot() == 30)
			{
				if (isBoots(event.getCursor().getType()))
				{
					event.setCurrentItem(event.getCursor());
					player.setItemOnCursor(null);
				}
			}
			event.setCancelled(true);
		}

		if (Arrays.asList(36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53).contains(event.getRawSlot()))
		{
			event.setCancelled(true);
		}
	}

	private void EditInventory(Player player, Kit kit, InventoryClickEvent event) {
		ItemStack itemStack = event.getCurrentItem();
		Inventory inventory = event.getInventory();

		if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
		{
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
			if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_edit"))))
			{
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

				event.setCancelled(true);
				player.closeInventory();

				Title.sendTitle(player, 2, 20, 2, "", ChatColor.GREEN + tl("kit_edit"));
			}

			if (itemName.equalsIgnoreCase(ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_cancel"))))
			{
				event.setCancelled(true);
				player.closeInventory();
			}
		}

		if (Arrays.asList(27, 28, 29, 30).contains(event.getRawSlot()))
		{
			if (event.getCursor() == null || event.getCursor().getType() == Material.AIR)
			{
				event.setCurrentItem(new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 13).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
			}

			if (event.getRawSlot() == 27)
			{
				if (isHelmet(event.getCursor().getType()))
				{
					event.setCurrentItem(event.getCursor());
					player.setItemOnCursor(null);
				}
			}
			else if (event.getRawSlot() == 28)
			{
				if (isChestplate(event.getCursor().getType()))
				{
					event.setCurrentItem(event.getCursor());
					player.setItemOnCursor(null);
				}
			}
			else if (event.getRawSlot() == 29)
			{
				if (isLeggings(event.getCursor().getType()))
				{
					event.setCurrentItem(event.getCursor());
					player.setItemOnCursor(null);
				}
			}
			else if (event.getRawSlot() == 30)
			{
				if (isBoots(event.getCursor().getType()))
				{
					event.setCurrentItem(event.getCursor());
					player.setItemOnCursor(null);
				}
			}
			event.setCancelled(true);
		}

		if (Arrays.asList(36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53).contains(event.getRawSlot()))
		{
			event.setCancelled(true);
		}
	}
}
