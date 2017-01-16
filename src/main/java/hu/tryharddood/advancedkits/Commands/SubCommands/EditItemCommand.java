package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Utils.ReflectionHelper.minecraft.Minecraft;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

import static hu.tryharddood.advancedkits.Utils.Localization.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class EditItemCommand extends Subcommand {
	@Override
	public String getPermission() {
		return Variables.KITADMIN_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit edititem <name|addlore|dellore|amount|durability>  [value]";
	}

	@Override
	public String getDescription() {
		return "Sets an item's variable.";
	}

	@Override
	public int getArgs() {
		return -1;
	}

	@Override
	public boolean playerOnly() {
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		if (args.length <= 2)
		{
			sender.sendMessage(tl("chat_usage") + ":");
			sender.sendMessage(ChatColor.GREEN + getUsage() + ChatColor.GRAY + " - " + ChatColor.BLUE + getDescription());
			return;
		}

		ItemStack itemStack = AdvancedKits.ServerVersion.newerThan(Minecraft.Version.v1_9_R1) ? player.getInventory().getItemInMainHand() : player.getItemInHand();

		if (itemStack == null || itemStack.getType() == Material.AIR)
		{
			sendMessage(player, tl("not_air"), ChatColor.RED);
			return;
		}

		String[] strings  = Arrays.copyOfRange(args, 1, args.length);
		ItemMeta itemMeta = itemStack.getItemMeta();

		if (strings[0].equalsIgnoreCase("name"))
		{
			String name = strings[1];
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		}
		else if (strings[0].equalsIgnoreCase("addlore"))
		{
			String       value = getArgString(strings, 1);
			List<String> lore;

			lore = itemMeta.getLore();
			lore.add(ChatColor.translateAlternateColorCodes('&', value));
			itemMeta.setLore(lore);
		}
		else if (strings[0].equalsIgnoreCase("dellore"))
		{
			String value = getArgString(strings, 1);

			if (!isNumeric(value))
			{
				player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_integer"));
				return;
			}
			List<String> lore = itemMeta.getLore();
			if (lore.size() <= Integer.valueOf(value))
			{
				player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + ChatColor.RED + "Cannot find the selected lore.");
				return;
			}

			lore.remove((int) Integer.valueOf(value));
			itemMeta.setLore(lore);
		}
		else if (strings[0].equalsIgnoreCase("durability"))
		{
			String value = getArgString(strings, 1);

			if (!isNumeric(value))
			{
				player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_integer"));
				return;
			}
			itemStack.setDurability(Short.valueOf(value));
		}
		else if (strings[0].equalsIgnoreCase("amount"))
		{
			String value = getArgString(strings, 1);

			if (!isNumeric(value))
			{
				player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_integer"));
				return;
			}
			itemStack.setAmount(Integer.valueOf(value));
		}
		itemStack.setItemMeta(itemMeta);
		player.updateInventory();
	}
}