package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.MainCommand;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static hu.tryharddood.advancedkits.Utils.Localization.I18n.tl;


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

		MainCommand.openViewInventory(player, kit);
	}
}
