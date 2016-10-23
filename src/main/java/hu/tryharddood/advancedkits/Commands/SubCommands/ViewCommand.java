package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static hu.tryharddood.advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class ViewCommand extends Subcommand {

	@Override
	public String getPermission() {
		return Variables.KIT_VIEW_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit view <kit>";
	}

	@Override
	public String getDescription() {
		return "Opens up the kit view for a specified kit";
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
			sendMessage(sender, tl("error_kit_not_found"), ChatColor.RED);
			return;
		}

		if (!player.hasPermission(Variables.KIT_VIEW_PERMISSION_KIT.replace("[kitname]", kit.getName())) && !player.hasPermission(Variables.KIT_VIEW_PERMISSION_ALL))
		{
			sendMessage(player, tl("error_no_permission"), ChatColor.RED);
			return;
		}

		List<ItemStack> itemStackList = kit.getItemStacks();
		List<ItemStack> armor         = kit.getArmor();

		int inventorySize = 54;

		/*PageInventory inv = new PageInventory(player);

		ItemStack[] items = itemStackList.toArray(new ItemStack[inventorySize]);

		if (player.hasPermission(Variables.KITADMIN_PERMISSION))
		{
			items[inventorySize - 9] = new ItemBuilder(Material.BOOK_AND_QUILL).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_edit"))).build();
			items[inventorySize - 1] = new ItemBuilder(Material.BARRIER).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("gui_button_delete"))).build();
		}

		{
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
			items[i] = new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 15).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build();
		}

		inv.setPages(items);
		inv.setTitle("Details - " + kit.getName());
		inv.openInventory();*/
	}
}
