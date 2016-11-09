package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Utils.Minecraft;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static hu.tryharddood.advancedkits.MenuBuilder.ItemBuilder.*;
import static hu.tryharddood.advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class UseCommand extends Subcommand {
	public static void GiveItems(Player player, Kit kit, boolean force) {
		if (!force && kit.getUses() > 0)
		{
			AdvancedKits.getKitManager().setUses(kit, player, (AdvancedKits.getKitManager().getUses(kit, player) + 1));
		}

		PlayerInventory inv = player.getInventory();

		int freespace = 0;
		int spaceneed = kit.getItemStacks().size();
		int kitarmor  = kit.getArmor().size();

		ItemStack[] giveArmorAsItem = new ItemStack[]{null, null, null, null};

		for (int c = 0; c < inv.getSize() - 1; c++)
		{
			if (inv.getItem(c) == null || inv.getItem(c).getType() == Material.AIR)
			{
				freespace++;
			}
		}

		if (!kit.getReplaceArmor())
		{
			if (kitarmor >= 1 && inv.getHelmet() != null)
			{
				giveArmorAsItem[0] = inv.getHelmet();
				spaceneed++;
			}

			if (kitarmor >= 2 && inv.getChestplate() != null)
			{
				giveArmorAsItem[1] = inv.getChestplate();
				spaceneed++;
			}

			if (kitarmor >= 3 && inv.getLeggings() != null)
			{
				giveArmorAsItem[2] = inv.getLeggings();
				spaceneed++;
			}

			if (kitarmor >= 4 && inv.getBoots() != null)
			{
				giveArmorAsItem[3] = inv.getBoots();
				spaceneed++;
			}
		}

		if (freespace < spaceneed)
		{
			player.sendMessage("You don't have enough space for the items. (" + spaceneed + ")");
			return;
		}

		if (kit.isClearinv())
		{
			if (AdvancedKits.ServerVersion.newerThan(Minecraft.Version.v1_9_R1))
			{
				inv.setArmorContents(null);
				inv.setExtraContents(null);
				inv.setItemInMainHand(null);
				inv.setItemInOffHand(null);
				inv.clear();
			}
			else
			{
				inv.setArmorContents(null);
				inv.clear();
			}
		}

		for (ItemStack item : kit.getArmor())
		{
			item = replaceWildcards(player, item);

			if (isHelmet(item.getType()))
			{
				inv.setHelmet(item);
			}
			else if (isChestplate(item.getType()))
			{
				inv.setChestplate(item);
			}
			else if (isLeggings(item.getType()))
			{
				inv.setLeggings(item);
			}
			else if (isBoots(item.getType()))
			{
				inv.setBoots(item);
			}
		}

		for (ItemStack prevArmor : giveArmorAsItem)
		{
			if (prevArmor != null) inv.addItem(prevArmor);
		}

		for (ItemStack item : kit.getItemStacks())
		{
			inv.addItem(replaceWildcards(player, item));
		}

		player.updateInventory();

		AdvancedKits.getKitManager().setDelay(player, kit.getDelay(), kit);
		sendMessage(player, tl("kituse_success"), ChatColor.GREEN);

		for (String command : kit.getCommands())
		{
			Bukkit.dispatchCommand(AdvancedKits.getInstance().getServer().getConsoleSender(), command.replaceAll("%player%", player.getName()));
		}
	}

	private static ItemStack replaceWildcards(Player player, ItemStack item) {
		if (item.hasItemMeta())
		{
			ItemMeta itemMeta = item.getItemMeta();
			if (itemMeta.hasDisplayName() && itemMeta.getDisplayName().contains("%player%"))
			{
				itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%player%", player.getName()));
			}

			if (itemMeta.getLore() != null)
			{
				List<String> lore = itemMeta.getLore();
				for (int i = 0; i < lore.size(); i++)
				{
					lore.set(i, lore.get(i).replaceAll("%player%", player.getName()));
				}
				itemMeta.setLore(lore);
			}

			item.setItemMeta(itemMeta);
		}
		return item;
	}

	@Override
	public String getPermission() {
		return Variables.KIT_USE_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit use <kit>";
	}

	@Override
	public String getDescription() {
		return "Uses a kit";
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
		if (kit.getUses() > 0 && (kit.getUses() - AdvancedKits.getKitManager().getUses(kit, player)) <= 0 && !player.hasPermission(Variables.KITADMIN_PERMISSION))
		{
			sendMessage(player, tl("cant_use_anymore"), ChatColor.RED);
			return;
		}

		if (AdvancedKits.getConfiguration().isEconomy() && (!kit.getDefaultUnlock() && !AdvancedKits.getKitManager().getUnlocked(kit, player)))
		{
			sendMessage(player, tl("kituse_error_notunlocked"), ChatColor.RED);
			return;
		}

		if (!player.hasPermission(Variables.KIT_USE_KIT_PERMISSION_ALL))
		{
			if (!player.hasPermission(kit.getPermission()))
			{
				sendMessage(player, tl("error_no_permission"), ChatColor.RED);
				return;
			}
		}

		if (kit.getDelay() > 0)
		{
			if (!player.hasPermission(Variables.KITDELAY_BYPASS))
			{
				if (!AdvancedKits.getKitManager().CheckCooldown(player, kit))
				{
					sendMessage(player, tl("kituse_wait", AdvancedKits.getKitManager().getDelay(player, kit)), ChatColor.RED);
					return;
				}
			}
		}

		if (kit.getWorlds().contains(player.getWorld().getName()))
		{
			sendMessage(player, tl("kitadmin_flag_world"), ChatColor.RED);
			return;
		}

		GiveItems(player, kit, false);
	}
}
